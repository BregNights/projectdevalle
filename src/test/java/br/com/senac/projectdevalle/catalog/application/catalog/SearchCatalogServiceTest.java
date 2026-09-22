package br.com.senac.projectdevalle.catalog.application.catalog;

import br.com.senac.projectdevalle.catalog.application.catalog.command.SearchCatalogCommand;
import br.com.senac.projectdevalle.catalog.application.port.CatalogSettingsPort;
import br.com.senac.projectdevalle.catalog.application.port.ProducerCatalogInfo;
import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.catalog.application.port.RestaurantDirectoryPort;
import br.com.senac.projectdevalle.catalog.domain.offer.CatalogFilter;
import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.Recurrence;
import br.com.senac.projectdevalle.shared.application.port.DistanceCalculationPort;
import br.com.senac.projectdevalle.shared.application.port.EstimatedDistance;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchCatalogServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ProducerDirectoryPort producerDirectoryPort;

    @Mock
    private RestaurantDirectoryPort restaurantDirectoryPort;

    @Mock
    private DistanceCalculationPort distanceCalculationPort;

    @Mock
    private CatalogSettingsPort catalogSettingsPort;

    private SearchCatalogService service;

    @BeforeEach
    void setUp() {
        service = new SearchCatalogService(offerRepository, producerDirectoryPort, restaurantDirectoryPort,
                distanceCalculationPort, catalogSettingsPort, FIXED_CLOCK);
        lenient().when(catalogSettingsPort.enabledCategories()).thenReturn(EnumSet.allOf(ProductCategory.class));
    }

    @Test
    void excludesOffersThatAreNotVisibleInCatalog() {
        Offer active = anOffer();
        Offer removed = anOffer();
        removed.remove();
        when(offerRepository.search(any())).thenReturn(List.of(active, removed));
        when(producerDirectoryPort.findById(any())).thenReturn(Optional.empty());

        List<CatalogEntry> entries = service.search(command(null, null, null, null));

        assertThat(entries).extracting(entry -> entry.offer().id()).containsExactly(active.id());
    }

    @Test
    void resolvesEligibleProducerIdsWhenFilteringByCityOrCertification() {
        UUID eligibleProducerId = UUID.randomUUID();
        when(offerRepository.search(any())).thenReturn(List.of());
        when(producerDirectoryPort.findEligibleProducerIds(Set.of("Blumenau"), "ORGANIC"))
                .thenReturn(Set.of(eligibleProducerId));

        service.search(command("Blumenau", "ORGANIC", null, null));

        verify(offerRepository).search(new CatalogFilter(null, null, Set.of(eligibleProducerId), null, null, null));
    }

    // RN01 — mesmo buscando um produtor específico, só aparecem ofertas se ele estiver apto a operar;
    // cidade/certificação são ignoradas nesse caso.
    @Test
    void restrictsExactProducerSearchToEligibleProducersIgnoringCityFilter() {
        UUID producerId = UUID.randomUUID();
        when(offerRepository.search(any())).thenReturn(List.of());
        when(producerDirectoryPort.findEligibleProducerIds(null, null)).thenReturn(Set.of(producerId));

        service.search(new SearchCatalogCommand(null, producerId, null, "Blumenau", null, null, null, null, null));

        verify(offerRepository).search(new CatalogFilter(null, producerId, Set.of(producerId), null, null, null));
    }

    // RN01 — sem nenhum filtro, ofertas de produtores suspensos/removidos continuam fora do catálogo.
    @Test
    void alwaysRestrictsToEligibleProducersEvenWithoutFilters() {
        UUID eligibleProducerId = UUID.randomUUID();
        when(offerRepository.search(any())).thenReturn(List.of());
        when(producerDirectoryPort.findEligibleProducerIds(null, null)).thenReturn(Set.of(eligibleProducerId));

        service.search(command(null, null, null, null));

        verify(offerRepository).search(new CatalogFilter(null, null, Set.of(eligibleProducerId), null, null, null));
    }

    // RF10 — filtro por região = produtores dos municípios daquela região.
    @Test
    void filtersByRegionUsingItsCities() {
        when(offerRepository.search(any())).thenReturn(List.of());
        when(catalogSettingsPort.citiesOfRegion("Vale do Itajaí")).thenReturn(Optional.of(Set.of("Blumenau", "Gaspar")));

        service.search(new SearchCatalogCommand(null, null, "Vale do Itajaí", null, null, null, null, null, null));

        verify(producerDirectoryPort).findEligibleProducerIds(Set.of("Blumenau", "Gaspar"), null);
    }

    @Test
    void cityOutsideTheChosenRegionYieldsNoProducers() {
        when(offerRepository.search(any())).thenReturn(List.of());
        when(catalogSettingsPort.citiesOfRegion("Litoral Norte")).thenReturn(Optional.of(Set.of("Itajaí")));

        service.search(new SearchCatalogCommand(null, null, "Litoral Norte", "Blumenau", null, null, null, null, null));

        verify(producerDirectoryPort).findEligibleProducerIds(Set.of(), null);
    }

    @Test
    void unknownRegionYieldsNoProducers() {
        when(offerRepository.search(any())).thenReturn(List.of());
        when(catalogSettingsPort.citiesOfRegion("Serra")).thenReturn(Optional.empty());

        service.search(new SearchCatalogCommand(null, null, "Serra", null, null, null, null, null, null));

        verify(producerDirectoryPort).findEligibleProducerIds(Set.of(), null);
    }

    // RF43 — ofertas de categorias desabilitadas pela administração saem do catálogo.
    @Test
    void hidesOffersOfDisabledCategories() {
        Offer grains = anOffer();
        when(catalogSettingsPort.enabledCategories()).thenReturn(EnumSet.of(ProductCategory.FISH));
        when(offerRepository.search(any())).thenReturn(List.of(grains));

        assertThat(service.search(command(null, null, null, null))).isEmpty();
    }

    @Test
    void forwardsAvailableByDeadlineToTheRepositoryFilter() {
        LocalDate deadline = LocalDate.of(2026, 6, 20);
        when(offerRepository.search(any())).thenReturn(List.of());

        service.search(new SearchCatalogCommand(null, null, null, null, null, null, null, deadline, null));

        verify(offerRepository).search(new CatalogFilter(null, null, Set.of(), null, null, deadline));
    }

    @Test
    void returnsNullDistanceForAnonymousSearch() {
        Offer offer = anOffer();
        when(offerRepository.search(any())).thenReturn(List.of(offer));
        when(producerDirectoryPort.findById(offer.producerId())).thenReturn(Optional.of(
                producerAt(offer.producerId(), new Coordinates(-26.9, -48.6))));

        List<CatalogEntry> entries = service.search(command(null, null, null, null));

        assertThat(entries.get(0).distance()).isNull();
        verify(distanceCalculationPort, never()).calculate(any(), any());
    }

    @Test
    void computesDistanceWhenRequesterIsARestaurantWithResolvedCoordinates() throws GeolocationUnavailableException {
        UUID requesterUserId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Coordinates restaurantCoordinates = new Coordinates(-26.9, -48.6);
        Coordinates producerCoordinates = new Coordinates(-26.2, -48.8);
        Offer offer = anOffer();
        EstimatedDistance expectedDistance = new EstimatedDistance(12.5, Duration.ofMinutes(20));

        when(offerRepository.search(any())).thenReturn(List.of(offer));
        when(producerDirectoryPort.findById(offer.producerId()))
                .thenReturn(Optional.of(producerAt(offer.producerId(), producerCoordinates)));
        when(restaurantDirectoryPort.findRestaurantIdByUserId(requesterUserId)).thenReturn(Optional.of(restaurantId));
        when(restaurantDirectoryPort.findPrimaryAddressCoordinates(restaurantId))
                .thenReturn(Optional.of(restaurantCoordinates));
        when(distanceCalculationPort.calculate(restaurantCoordinates, producerCoordinates))
                .thenReturn(expectedDistance);

        List<CatalogEntry> entries = service.search(
                new SearchCatalogCommand(null, null, null, null, null, null, null, null, requesterUserId));

        assertThat(entries.get(0).distance()).isEqualTo(expectedDistance);
    }

    // RNF11 — indisponibilidade do cálculo de distância nunca pode impedir a exibição da oferta.
    @Test
    void degradesGracefullyWhenDistanceCalculationFails() throws GeolocationUnavailableException {
        UUID requesterUserId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        Offer offer = anOffer();
        Coordinates coordinates = new Coordinates(-26.9, -48.6);

        when(offerRepository.search(any())).thenReturn(List.of(offer));
        when(producerDirectoryPort.findById(offer.producerId()))
                .thenReturn(Optional.of(producerAt(offer.producerId(), coordinates)));
        when(restaurantDirectoryPort.findRestaurantIdByUserId(requesterUserId)).thenReturn(Optional.of(restaurantId));
        when(restaurantDirectoryPort.findPrimaryAddressCoordinates(restaurantId)).thenReturn(Optional.of(coordinates));
        when(distanceCalculationPort.calculate(any(), any())).thenThrow(new GeolocationUnavailableException("timeout"));

        List<CatalogEntry> entries = service.search(
                new SearchCatalogCommand(null, null, null, null, null, null, null, null, requesterUserId));

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).distance()).isNull();
    }

    private SearchCatalogCommand command(String city, String certificationType, BigDecimal minPrice,
                                          BigDecimal maxPrice) {
        return new SearchCatalogCommand(null, null, null, city, certificationType, minPrice, maxPrice, null, null);
    }

    private static Offer anOffer() {
        return Offer.publish(UUID.randomUUID(), "Arroz", ProductCategory.GRAINS_CEREALS, MeasurementUnit.KILOGRAM,
                BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(), null, List.of());
    }

    private static ProducerCatalogInfo producerAt(UUID producerId, Coordinates coordinates) {
        return new ProducerCatalogInfo(producerId, "Sitio Vale Verde", "Blumenau", coordinates, List.of());
    }
}
