package br.com.senac.projectdevalle.catalog.application.offer;

import br.com.senac.projectdevalle.catalog.application.offer.command.UpdateOfferDetailsCommand;
import br.com.senac.projectdevalle.catalog.domain.offer.AvailabilityWindow;
import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.Offer;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferRepository;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.Recurrence;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.ExpiredAvailabilityWindowException;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.MissingAvailabilityDeadlineException;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOfferDetailsServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private OfferOwnershipResolver ownershipResolver;

    private UpdateOfferDetailsService service;

    @BeforeEach
    void setUp() {
        service = new UpdateOfferDetailsService(offerRepository, ownershipResolver, FIXED_CLOCK);
    }

    @Test
    void updatesPriceAndPersistsTheOffer() {
        UUID userId = UUID.randomUUID();
        UUID offerId = UUID.randomUUID();
        Offer offer = anActiveOffer();
        when(ownershipResolver.resolveOwnedOfferForEditing(offerId, userId)).thenReturn(offer);

        service.update(new UpdateOfferDetailsCommand(offerId, userId, BigDecimal.valueOf(19.9), null, List.of()));

        assertThat(offer.price()).isEqualByComparingTo("19.9");
        verify(offerRepository).save(offer);
    }

    // RN06
    @Test
    void rejectsRemovingAvailabilityDeadlineFromAPerishableOffer() {
        UUID userId = UUID.randomUUID();
        UUID offerId = UUID.randomUUID();
        Offer offer = Offer.publish(UUID.randomUUID(), "Tomate", ProductCategory.VEGETABLES,
                MeasurementUnit.KILOGRAM, BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(),
                new AvailabilityWindow(null, LocalDate.of(2026, 12, 31)), List.of());
        when(ownershipResolver.resolveOwnedOfferForEditing(offerId, userId)).thenReturn(offer);

        assertThatThrownBy(() -> service.update(new UpdateOfferDetailsCommand(offerId, userId, BigDecimal.TEN,
                null, List.of())))
                .isInstanceOf(MissingAvailabilityDeadlineException.class);
    }

    // RN48
    @Test
    void rejectsMovingTheDeadlineToAPastDate() {
        UUID userId = UUID.randomUUID();
        UUID offerId = UUID.randomUUID();
        Offer offer = anActiveOffer();
        when(ownershipResolver.resolveOwnedOfferForEditing(offerId, userId)).thenReturn(offer);

        assertThatThrownBy(() -> service.update(new UpdateOfferDetailsCommand(offerId, userId, BigDecimal.TEN,
                new AvailabilityWindow(null, LocalDate.of(2026, 6, 1)), List.of())))
                .isInstanceOf(ExpiredAvailabilityWindowException.class);
    }

    private static Offer anActiveOffer() {
        return Offer.publish(UUID.randomUUID(), "Arroz", ProductCategory.GRAINS_CEREALS, MeasurementUnit.KILOGRAM,
                BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(), null, List.of());
    }
}
