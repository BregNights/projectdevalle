package br.com.senac.projectdevalle.catalog.domain.offer;

import br.com.senac.projectdevalle.catalog.domain.offer.exception.InsufficientStockException;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.InvalidOfferTransitionException;
import br.com.senac.projectdevalle.catalog.domain.offer.exception.MissingAvailabilityDeadlineException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfferTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void publishFailsWithBlankProductName() {
        assertThatThrownBy(() -> Offer.publish(UUID.randomUUID(), " ", ProductCategory.GRAINS_CEREALS,
                MeasurementUnit.KILOGRAM, BigDecimal.TEN, BigDecimal.ONE, Recurrence.oneTime(), null, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void publishFailsWithZeroQuantity() {
        assertThatThrownBy(() -> Offer.publish(UUID.randomUUID(), "Arroz", ProductCategory.GRAINS_CEREALS,
                MeasurementUnit.KILOGRAM, BigDecimal.TEN, BigDecimal.ZERO, Recurrence.oneTime(), null, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void publishFailsWithZeroPrice() {
        assertThatThrownBy(() -> Offer.publish(UUID.randomUUID(), "Arroz", ProductCategory.GRAINS_CEREALS,
                MeasurementUnit.KILOGRAM, BigDecimal.ZERO, BigDecimal.TEN, Recurrence.oneTime(), null, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // RN06
    @Test
    void publishFailsForPerishableCategoryWithoutAvailabilityDeadline() {
        assertThatThrownBy(() -> Offer.publish(UUID.randomUUID(), "Tomate", ProductCategory.VEGETABLES,
                MeasurementUnit.KILOGRAM, BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(), null, List.of()))
                .isInstanceOf(MissingAvailabilityDeadlineException.class);
    }

    @Test
    void publishSucceedsForPerishableCategoryWithAvailabilityDeadline() {
        Offer offer = publishPerishable(BigDecimal.TEN);

        assertThat(offer.status()).isEqualTo(OfferStatus.ACTIVE);
    }

    @Test
    void publishSucceedsForNonPerishableCategoryWithoutAvailabilityWindow() {
        Offer offer = Offer.publish(UUID.randomUUID(), "Arroz", ProductCategory.GRAINS_CEREALS,
                MeasurementUnit.KILOGRAM, BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(), null, List.of());

        assertThat(offer.status()).isEqualTo(OfferStatus.ACTIVE);
    }

    @Test
    void pauseFailsWhenOfferIsNotActive() {
        Offer offer = publishNonPerishable(BigDecimal.TEN);
        offer.remove();

        assertThatThrownBy(offer::pause).isInstanceOf(InvalidOfferTransitionException.class);
    }

    @Test
    void resumeFromPausedGoesBackToActiveWhenThereIsQuantity() {
        Offer offer = publishNonPerishable(BigDecimal.TEN);
        offer.pause();

        offer.resume();

        assertThat(offer.status()).isEqualTo(OfferStatus.ACTIVE);
    }

    @Test
    void removeIsIdempotentGuardedAgainstDoubleRemoval() {
        Offer offer = publishNonPerishable(BigDecimal.TEN);
        offer.remove();

        assertThatThrownBy(offer::remove).isInstanceOf(InvalidOfferTransitionException.class);
    }

    // RN04
    @Test
    void updateQuantityToZeroMarksOfferAsSoldOut() {
        Offer offer = publishNonPerishable(BigDecimal.TEN);

        offer.updateQuantity(BigDecimal.ZERO);

        assertThat(offer.status()).isEqualTo(OfferStatus.SOLD_OUT);
    }

    // RN04
    @Test
    void restockingASoldOutOfferReactivatesItAutomatically() {
        Offer offer = publishNonPerishable(BigDecimal.TEN);
        offer.updateQuantity(BigDecimal.ZERO);

        offer.updateQuantity(BigDecimal.TEN);

        assertThat(offer.status()).isEqualTo(OfferStatus.ACTIVE);
    }

    @Test
    void updateDetailsFailsWhenRemovingAvailabilityDeadlineFromPerishableOffer() {
        Offer offer = publishPerishable(BigDecimal.TEN);

        assertThatThrownBy(() -> offer.updateDetails(BigDecimal.TEN, null, List.of()))
                .isInstanceOf(MissingAvailabilityDeadlineException.class);
    }

    @Test
    void isExpiredWhenAvailabilityUntilIsInThePast() {
        Offer offer = Offer.publish(UUID.randomUUID(), "Peixe", ProductCategory.FISH, MeasurementUnit.KILOGRAM,
                BigDecimal.TEN, BigDecimal.TEN, Recurrence.oneTime(),
                new AvailabilityWindow(null, LocalDate.of(2026, 6, 1)), List.of());

        assertThat(offer.isExpired(FIXED_CLOCK)).isTrue();
        assertThat(offer.isVisibleInCatalog(FIXED_CLOCK)).isFalse();
    }

    @Test
    void recurrenceRequiresDayOfWeekOnlyWhenRecurring() {
        assertThatThrownBy(() -> new Recurrence(RecurrenceType.RECURRING, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Recurrence(RecurrenceType.ONE_TIME, DayOfWeek.MONDAY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // Pedidos — confirmar um pedido baixa o estoque; zerar leva a SOLD_OUT (RN04).
    @Test
    void reservingStockDecrementsAndSellsOutAtZero() {
        Offer offer = publishNonPerishable(BigDecimal.TEN);

        offer.reserve(BigDecimal.valueOf(4));
        assertThat(offer.quantityAvailable()).isEqualByComparingTo("6");

        offer.reserve(BigDecimal.valueOf(6));
        assertThat(offer.status()).isEqualTo(OfferStatus.SOLD_OUT);
    }

    @Test
    void reservingMoreThanAvailableFails() {
        Offer offer = publishNonPerishable(BigDecimal.ONE);

        assertThatThrownBy(() -> offer.reserve(BigDecimal.TEN)).isInstanceOf(InsufficientStockException.class);
        assertThat(offer.quantityAvailable()).isEqualByComparingTo("1");
    }

    @Test
    void releasingStockRestoresQuantityAndReactivatesButIgnoresRemovedOffers() {
        Offer offer = publishNonPerishable(BigDecimal.ONE);
        offer.reserve(BigDecimal.ONE);

        offer.release(BigDecimal.ONE);
        assertThat(offer.status()).isEqualTo(OfferStatus.ACTIVE);

        offer.remove();
        offer.release(BigDecimal.TEN);
        assertThat(offer.quantityAvailable()).isEqualByComparingTo("1");
    }

    private static Offer publishNonPerishable(BigDecimal quantity) {
        return Offer.publish(UUID.randomUUID(), "Arroz", ProductCategory.GRAINS_CEREALS, MeasurementUnit.KILOGRAM,
                BigDecimal.TEN, quantity, Recurrence.oneTime(), null, List.of());
    }

    private static Offer publishPerishable(BigDecimal quantity) {
        return Offer.publish(UUID.randomUUID(), "Tomate", ProductCategory.VEGETABLES, MeasurementUnit.KILOGRAM,
                BigDecimal.TEN, quantity, Recurrence.oneTime(),
                new AvailabilityWindow(null, LocalDate.of(2026, 7, 1)), List.of());
    }
}
