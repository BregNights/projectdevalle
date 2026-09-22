package br.com.senac.projectdevalle.catalog.application.catalog;

import br.com.senac.projectdevalle.catalog.application.catalog.command.SearchCatalogCommand;
import br.com.senac.projectdevalle.catalog.application.port.CatalogSettingsPort;
import br.com.senac.projectdevalle.catalog.application.port.ProducerCatalogInfo;
import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.catalog.application.port.RestaurantDirectoryPort;
import br.com.senac.projectdevalle.catalog.domain.offer.CatalogFilter;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.shared.application.port.DistanceCalculationPort;
import br.com.senac.projectdevalle.shared.application.port.EstimatedDistance;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.PlaceNames;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// RF10 (busca/filtros do catálogo) + RF11 (distância até o restaurante, quando aplicável).
@Service
public class SearchCatalogService {

    private final OfferRepository offerRepository;
    private final ProducerDirectoryPort producerDirectoryPort;
    private final RestaurantDirectoryPort restaurantDirectoryPort;
    private final DistanceCalculationPort distanceCalculationPort;
    private final CatalogSettingsPort catalogSettingsPort;
    private final Clock clock;

    public SearchCatalogService(OfferRepository offerRepository, ProducerDirectoryPort producerDirectoryPort,
                                 RestaurantDirectoryPort restaurantDirectoryPort,
                                 DistanceCalculationPort distanceCalculationPort,
                                 CatalogSettingsPort catalogSettingsPort, Clock clock) {
        this.offerRepository = offerRepository;
        this.producerDirectoryPort = producerDirectoryPort;
        this.restaurantDirectoryPort = restaurantDirectoryPort;
        this.distanceCalculationPort = distanceCalculationPort;
        this.catalogSettingsPort = catalogSettingsPort;
        this.clock = clock;
    }

    public List<CatalogEntry> search(SearchCatalogCommand command) {
        CatalogFilter filter = buildFilter(command);
        Coordinates requesterCoordinates = resolveRequesterCoordinates(command.requesterUserId());
        // RF43 — categorias desabilitadas pela administração saem do catálogo.
        Set<ProductCategory> enabledCategories = catalogSettingsPort.enabledCategories();

        return offerRepository.search(filter).stream()
                .filter(offer -> offer.isVisibleInCatalog(clock))
                .filter(offer -> enabledCategories.contains(offer.category()))
                .map(offer -> toCatalogEntry(offer, requesterCoordinates))
                .toList();
    }

    // RN01/RN02 — o catálogo sempre se restringe a produtores aptos a operar (aprovados): ofertas de
    // produtores suspensos ou removidos somem da busca, com ou sem filtro de cidade/certificação.
    private CatalogFilter buildFilter(SearchCatalogCommand command) {
        boolean byProducer = command.producerId() != null;
        Set<String> cities = byProducer ? null : resolveCities(command.region(), command.city());
        String certificationType = byProducer ? null : command.certificationType();
        Set<UUID> eligibleProducerIds = producerDirectoryPort.findEligibleProducerIds(cities, certificationType);
        return new CatalogFilter(command.category(), command.producerId(), eligibleProducerIds, command.minPrice(),
                command.maxPrice(), command.availableBy());
    }

    // RF10 — filtro por região (conjunto de municípios) e/ou município. Região desconhecida, ou município fora
    // da região escolhida, resulta em nenhum município (busca vazia) em vez de ignorar o filtro.
    private Set<String> resolveCities(String region, String city) {
        boolean hasRegion = region != null && !region.isBlank();
        boolean hasCity = city != null && !city.isBlank();
        if (!hasRegion) {
            return hasCity ? Set.of(city) : null;
        }
        Set<String> regionCities = catalogSettingsPort.citiesOfRegion(region).orElse(Set.of());
        if (!hasCity) {
            return regionCities;
        }
        return regionCities.stream().anyMatch(candidate -> PlaceNames.sameName(candidate, city))
                ? Set.of(city)
                : Set.of();
    }

    private Coordinates resolveRequesterCoordinates(UUID requesterUserId) {
        if (requesterUserId == null) {
            return null;
        }
        return restaurantDirectoryPort.findRestaurantIdByUserId(requesterUserId)
                .flatMap(restaurantDirectoryPort::findPrimaryAddressCoordinates)
                .orElse(null);
    }

    private CatalogEntry toCatalogEntry(Offer offer, Coordinates requesterCoordinates) {
        ProducerCatalogInfo producer = producerDirectoryPort.findById(offer.producerId()).orElse(null);
        EstimatedDistance distance = computeDistance(requesterCoordinates, producer);
        return new CatalogEntry(offer, producer, distance);
    }

    // RNF11 — falha ou indisponibilidade do cálculo de distância nunca impede a exibição da oferta.
    private EstimatedDistance computeDistance(Coordinates requesterCoordinates, ProducerCatalogInfo producer) {
        if (requesterCoordinates == null || producer == null || producer.coordinates() == null) {
            return null;
        }
        try {
            return distanceCalculationPort.calculate(requesterCoordinates, producer.coordinates());
        } catch (GeolocationUnavailableException exception) {
            return null;
        }
    }
}
