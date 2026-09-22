package br.com.senac.projectdevalle.ordering.domain.recurring;

import br.com.senac.projectdevalle.ordering.domain.order.exception.InvalidOrderOperationException;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrder.RecurringOrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecurringOrderTest {

    // Segunda-feira, 15/06/2026, 10h.
    private static final Clock MONDAY = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);
    private static final Duration NOTICE_48H = Duration.ofHours(48);

    // RF21 — a primeira entrega é o próximo dia da semana escolhido que respeite a antecedência de geração.
    @Test
    void schedulesFirstDeliveryRespectingTheGenerationLeadTime() {
        RecurringOrder thursday = create(DayOfWeek.THURSDAY);
        RecurringOrder tuesday = create(DayOfWeek.TUESDAY);

        assertThat(thursday.nextDeliveryDate()).isEqualTo(LocalDate.of(2026, 6, 18));
        assertThat(thursday.nextRunDate()).isEqualTo(LocalDate.of(2026, 6, 16));
        // Terça (16/06) não dá 2 dias de antecedência: vai para a terça seguinte.
        assertThat(tuesday.nextDeliveryDate()).isEqualTo(LocalDate.of(2026, 6, 23));
    }

    @Test
    void becomesDueOnTheRunDateAndAdvancesOneWeekAfterExecution() {
        RecurringOrder order = create(DayOfWeek.THURSDAY);
        Clock tuesday = Clock.offset(MONDAY, Duration.ofDays(1));

        assertThat(order.isDue(MONDAY)).isFalse();
        assertThat(order.isDue(tuesday)).isTrue();

        order.markExecuted("1 pedido gerado", tuesday);

        assertThat(order.nextDeliveryDate()).isEqualTo(LocalDate.of(2026, 6, 25));
        assertThat(order.lastRunSummary()).isEqualTo("1 pedido gerado");
        assertThat(order.isDue(tuesday)).isFalse();
    }

    // RN12 — com aviso de pelo menos 48h antes da próxima execução, suspende na hora.
    @Test
    void suspendsImmediatelyWithEnoughNotice() {
        RecurringOrder order = create(DayOfWeek.SATURDAY); // entrega 20/06, execução 18/06 00:00

        assertThat(order.suspend(NOTICE_48H, MONDAY)).isTrue();
        assertThat(order.status()).isEqualTo(RecurringOrderStatus.SUSPENDED);
    }

    // RN12 — sem o aviso mínimo, a próxima execução ainda acontece e a suspensão vale depois dela.
    @Test
    void suspensionWithinTheNoticeWindowTakesEffectAfterTheNextRun() {
        RecurringOrder order = create(DayOfWeek.THURSDAY); // execução 16/06 00:00, menos de 48h

        assertThat(order.suspend(NOTICE_48H, MONDAY)).isFalse();
        assertThat(order.status()).isEqualTo(RecurringOrderStatus.ACTIVE);
        assertThat(order.suspendAfterNextRun()).isTrue();

        order.markExecuted("gerado", Clock.offset(MONDAY, Duration.ofDays(1)));
        assertThat(order.status()).isEqualTo(RecurringOrderStatus.SUSPENDED);
    }

    @Test
    void resumeRecomputesTheNextDeliveryFromToday() {
        RecurringOrder order = create(DayOfWeek.SATURDAY);
        order.suspend(NOTICE_48H, MONDAY);
        Clock twoWeeksLater = Clock.offset(MONDAY, Duration.ofDays(14));

        order.resume(twoWeeksLater);

        assertThat(order.status()).isEqualTo(RecurringOrderStatus.ACTIVE);
        assertThat(order.nextDeliveryDate()).isEqualTo(LocalDate.of(2026, 7, 4));
        assertThatThrownBy(() -> order.resume(twoWeeksLater)).isInstanceOf(InvalidOrderOperationException.class);
    }

    @Test
    void requiresItemsWithoutRepeatedOffers() {
        UUID offer = UUID.randomUUID();
        assertThatThrownBy(() -> RecurringOrder.create(UUID.randomUUID(), UUID.randomUUID(), DayOfWeek.MONDAY,
                List.of(), null, MONDAY)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RecurringOrder.create(UUID.randomUUID(), UUID.randomUUID(), DayOfWeek.MONDAY,
                List.of(new RecurringOrderItem(offer, BigDecimal.ONE), new RecurringOrderItem(offer, BigDecimal.TEN)),
                null, MONDAY)).isInstanceOf(IllegalArgumentException.class);
    }

    private static RecurringOrder create(DayOfWeek day) {
        return RecurringOrder.create(UUID.randomUUID(), UUID.randomUUID(), day,
                List.of(new RecurringOrderItem(UUID.randomUUID(), BigDecimal.TEN)), null, MONDAY);
    }
}
