package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.PublishOfferCommand;
import br.com.senac.projectdevalle.catalog.application.port.CatalogSettingsPort;
import br.com.senac.projectdevalle.catalog.application.port.ProducerDirectoryPort;
import br.com.senac.projectdevalle.catalog.domain.offer.AvailabilityWindow;
import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.Recurrence;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.ExpiredAvailabilityWindowException;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.ProducerNotEligibleException;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.ProductCategoryNotEnabledException;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublishOfferServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private ProducerDirectoryPort producerDirectoryPort;

    @Mock
    private CatalogSettingsPort catalogSettingsPort;

    private PublishOfferService service;

    @BeforeEach
    void setUp() {
        service = new PublishOfferService(offerRepository, producerDirectoryPort, catalogSettingsPort, FIXED_CLOCK);
        lenient().when(catalogSettingsPort.enabledCategories()).thenReturn(EnumSet.allOf(ProductCategory.class));
    }

    @Test
    void rejectsPublishingWhenProducerIsNotEligibleToOperate() {
        UUID userId = UUID.randomUUID();
        UUID producerId = UUID.randomUUID();
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(producerId));
        when(producerDirectoryPort.isEligibleToOperate(producerId)).thenReturn(false);

        assertThatThrownBy(() -> service.publish(validCommand(userId)))
                .isInstanceOf(ProducerNotEligibleException.class);
    }

    @Test
    void rejectsPublishingWhenNoProducerIsRegisteredForCurrentUser() {
        UUID userId = UUID.randomUUID();
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publish(validCommand(userId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void publishesOfferForEligibleProducer() {
        UUID userId = UUID.randomUUID();
        UUID producerId = UUID.randomUUID();
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(producerId));
        when(producerDirectoryPort.isEligibleToOperate(producerId)).thenReturn(true);
        when(offerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Offer offer = service.publish(validCommand(userId));

        assertThat(offer.producerId()).isEqualTo(producerId);
    }

    // RN48 — oferta que já nasceria vencida é recusada em vez de ficar invisível no catálogo.
    @Test
    void rejectsPublishingAnOfferWhoseDeadlineHasAlreadyPassed() {
        UUID userId = UUID.randomUUID();
        UUID producerId = UUID.randomUUID();
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(producerId));
        when(producerDirectoryPort.isEligibleToOperate(producerId)).thenReturn(true);
        PublishOfferCommand expired = new PublishOfferCommand(userId, "Tomate", ProductCategory.VEGETABLES,
                MeasurementUnit.KILOGRAM, BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(),
                new AvailabilityWindow(null, LocalDate.of(2026, 6, 14)), List.of());

        assertThatThrownBy(() -> service.publish(expired)).isInstanceOf(ExpiredAvailabilityWindowException.class);
        verify(offerRepository, never()).save(any());
    }

    // RF43 — categoria desabilitada pela administração não aceita novas ofertas.
    @Test
    void rejectsPublishingInACategoryDisabledByTheAdministration() {
        UUID userId = UUID.randomUUID();
        UUID producerId = UUID.randomUUID();
        when(producerDirectoryPort.findProducerIdByUserId(userId)).thenReturn(Optional.of(producerId));
        when(producerDirectoryPort.isEligibleToOperate(producerId)).thenReturn(true);
        when(catalogSettingsPort.enabledCategories()).thenReturn(EnumSet.of(ProductCategory.FISH));

        assertThatThrownBy(() -> service.publish(validCommand(userId)))
                .isInstanceOf(ProductCategoryNotEnabledException.class);
        verify(offerRepository, never()).save(any());
    }

    private PublishOfferCommand validCommand(UUID userId) {
        return new PublishOfferCommand(userId, "Arroz", ProductCategory.GRAINS_CEREALS, MeasurementUnit.KILOGRAM,
                BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(), null, List.of());
    }
}
