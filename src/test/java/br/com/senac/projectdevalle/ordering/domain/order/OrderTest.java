package br.com.senac.projectdevalle.ordering.domain.order;

import br.com.senac.projectdevalle.ordering.domain.order.exception.InvalidOrderOperationException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TOMORROW = LocalDate.of(2026, 6, 16);
    private static final BigDecimal PENALTY_20 = BigDecimal.valueOf(20);

    // RF16/RF18 — o pedido nasce pendente, aguardando o produtor, com a 1ª rodada registrada.
    @Test
    void placedOrderAwaitsTheProducerWithOpeningRound() {
        Order order = anOrder(null);

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.awaitingResponseFrom()).isEqualTo(OrderParty.PRODUCER);
        assertThat(order.negotiation()).singleElement()
                .extracting(NegotiationRound::proposedBy).isEqualTo(OrderParty.RESTAURANT);
        assertThat(order.total()).isEqualByComparingTo("50.00");
    }

    @Test
    void restaurantCanProposeAPriceBelowTheListPrice() {
        Order order = anOrder(BigDecimal.valueOf(4));

        assertThat(order.items().get(0).listUnitPrice()).isEqualByComparingTo("5");
        assertThat(order.items().get(0).unitPrice()).isEqualByComparingTo("4");
        assertThat(order.total()).isEqualByComparingTo("40.00");
    }

    @Test
    void rejectsDeliveryDateInThePastAndDuplicatedOffers() {
        UUID offer = UUID.randomUUID();
        assertThatThrownBy(() -> Order.place(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), destination(),
                LocalDate.of(2026, 6, 14), null, List.of(item(offer, null)), null, CLOCK))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Order.place(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), destination(),
                TOMORROW, null, List.of(item(offer, null), item(offer, null)), null, CLOCK))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // RF18 — negociação alternada: só quem tem a vez aceita ou contrapropõe.
    @Test
    void negotiationAlternatesTurnsUntilSomeoneAccepts() {
        Order order = anOrder(BigDecimal.valueOf(4));
        UUID itemId = order.items().get(0).id();

        assertThatThrownBy(() -> order.accept(OrderParty.RESTAURANT, CLOCK))
                .isInstanceOf(InvalidOrderOperationException.class);

        order.counterPropose(OrderParty.PRODUCER, List.of(new ItemTerms(itemId, BigDecimal.valueOf(8),
                new BigDecimal("4.50"))), LocalDate.of(2026, 6, 18), "Só tenho 8 kg", CLOCK);

        assertThat(order.awaitingResponseFrom()).isEqualTo(OrderParty.RESTAURANT);
        assertThat(order.requestedDeliveryDate()).isEqualTo(LocalDate.of(2026, 6, 18));
        assertThat(order.total()).isEqualByComparingTo("36.00");
        assertThat(order.negotiation()).hasSize(2);

        order.accept(OrderParty.RESTAURANT, CLOCK);

        assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.awaitingResponseFrom()).isNull();
        assertThat(order.confirmedAt()).isNotNull();
    }

    @Test
    void counterProposalMustReferenceItemsOfTheOrder() {
        Order order = anOrder(null);

        assertThatThrownBy(() -> order.counterPropose(OrderParty.PRODUCER,
                List.of(new ItemTerms(UUID.randomUUID(), BigDecimal.ONE, BigDecimal.ONE)), null, null, CLOCK))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(order.awaitingResponseFrom()).isEqualTo(OrderParty.PRODUCER);
    }

    // RF19 — fluxo completo com rastreabilidade (RN23) e horários de coleta/entrega (RF30).
    @Test
    void fullLifecycleRecordsTraceabilityAndTimestamps() {
        Order order = confirmedOrder();
        UUID itemId = order.items().get(0).id();

        order.startPreparation(OrderParty.PRODUCER, CLOCK);
        assertThatThrownBy(() -> order.dispatch(OrderParty.PRODUCER, List.of(), CLOCK))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> order.dispatch(OrderParty.PRODUCER,
                List.of(new Traceability(itemId, TOMORROW, null)), CLOCK))
                .isInstanceOf(IllegalArgumentException.class);

        order.dispatch(OrderParty.PRODUCER, List.of(new Traceability(itemId, LocalDate.of(2026, 6, 14), "L-01")),
                CLOCK);
        assertThat(order.status()).isEqualTo(OrderStatus.IN_TRANSIT);
        assertThat(order.pickedUpAt()).isNotNull();
        assertThat(order.items().get(0).harvestDate()).isEqualTo(LocalDate.of(2026, 6, 14));
        assertThat(order.items().get(0).lot()).isEqualTo("L-01");

        assertThatThrownBy(() -> order.confirmReceipt(OrderParty.PRODUCER, CLOCK))
                .isInstanceOf(InvalidOrderOperationException.class);
        order.confirmReceipt(OrderParty.RESTAURANT, CLOCK);
        assertThat(order.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.deliveredAt()).isNotNull();
        assertThat(order.history()).extracting(StatusChange::status).containsExactly(OrderStatus.PENDING,
                OrderStatus.CONFIRMED, OrderStatus.IN_PREPARATION, OrderStatus.IN_TRANSIT, OrderStatus.DELIVERED);
    }

    @Test
    void onlyTheProducerMovesPreparationAndDispatch() {
        Order order = confirmedOrder();

        assertThatThrownBy(() -> order.startPreparation(OrderParty.RESTAURANT, CLOCK))
                .isInstanceOf(InvalidOrderOperationException.class);
    }

    // RN10 — cancelamento do restaurante durante o preparo gera multa proporcional.
    @Test
    void restaurantCancellingDuringPreparationPaysAProportionalPenalty() {
        Order order = confirmedOrder();
        order.startPreparation(OrderParty.PRODUCER, CLOCK);

        boolean heldStock = order.cancel(OrderParty.RESTAURANT, "Mudança de cardápio", PENALTY_20, CLOCK);

        assertThat(heldStock).isTrue();
        assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.cancellation().penaltyAmount()).isEqualByComparingTo("10.00");
        assertThat(order.cancellation().afterConfirmation()).isTrue();
    }

    @Test
    void restaurantCancellingAConfirmedOrderBeforePreparationPaysNothing() {
        Order order = confirmedOrder();

        order.cancel(OrderParty.RESTAURANT, "Evento cancelado", PENALTY_20, CLOCK);

        assertThat(order.cancellation().penaltyAmount()).isEqualByComparingTo("0");
    }

    // RN11 — produtor não paga multa, mas o cancelamento após confirmar fica registrado (taxa de cumprimento).
    @Test
    void producerCancellationHasNoPenaltyButIsMarkedAsAfterConfirmation() {
        Order order = confirmedOrder();
        order.startPreparation(OrderParty.PRODUCER, CLOCK);

        order.cancel(OrderParty.PRODUCER, "Mau tempo impediu a pesca", PENALTY_20, CLOCK);

        assertThat(order.cancellation().cancelledBy()).isEqualTo(OrderParty.PRODUCER);
        assertThat(order.cancellation().penaltyAmount()).isEqualByComparingTo("0");
        assertThat(order.cancellation().afterConfirmation()).isTrue();
    }

    @Test
    void rejectingAPendingOrderDoesNotHoldStockNorCountAgainstTheProducer() {
        Order order = anOrder(null);

        boolean heldStock = order.cancel(OrderParty.PRODUCER, "Sem produção esta semana", PENALTY_20, CLOCK);

        assertThat(heldStock).isFalse();
        assertThat(order.cancellation().afterConfirmation()).isFalse();
    }

    @Test
    void cannotCancelWithoutReasonOrAfterDispatch() {
        Order order = confirmedOrder();
        assertThatThrownBy(() -> order.cancel(OrderParty.RESTAURANT, " ", PENALTY_20, CLOCK))
                .isInstanceOf(IllegalArgumentException.class);

        order.dispatch(OrderParty.PRODUCER, List.of(new Traceability(order.items().get(0).id(),
                LocalDate.of(2026, 6, 15), null)), CLOCK);
        assertThatThrownBy(() -> order.cancel(OrderParty.RESTAURANT, "Desisti", PENALTY_20, CLOCK))
                .isInstanceOf(InvalidOrderOperationException.class);
    }

    // RN14 — sem confirmação do restaurante dentro do prazo, o recebimento é confirmado automaticamente.
    @Test
    void receiptIsAutoConfirmedAfterTheDeadline() {
        Order order = confirmedOrder();
        order.dispatch(OrderParty.PRODUCER, List.of(new Traceability(order.items().get(0).id(),
                LocalDate.of(2026, 6, 15), null)), CLOCK);

        assertThat(order.autoConfirmReceipt(Duration.ofHours(72), CLOCK)).isFalse();

        Clock fourDaysLater = Clock.offset(CLOCK, Duration.ofDays(4));
        assertThat(order.autoConfirmReceipt(Duration.ofHours(72), fourDaysLater)).isTrue();
        assertThat(order.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.deliveryAutoConfirmed()).isTrue();
    }

    private static Order confirmedOrder() {
        Order order = anOrder(null);
        order.accept(OrderParty.PRODUCER, CLOCK);
        return order;
    }

    private static Order anOrder(BigDecimal proposedUnitPrice) {
        return Order.place(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), destination(), TOMORROW,
                "Entregar pela manhã", List.of(item(UUID.randomUUID(), proposedUnitPrice)), null, CLOCK);
    }

    private static NewOrderItem item(UUID offerId, BigDecimal proposedUnitPrice) {
        return new NewOrderItem(offerId, "Alface", "VEGETABLES", "UNIT", BigDecimal.valueOf(5), BigDecimal.TEN,
                proposedUnitPrice);
    }

    private static DeliveryDestination destination() {
        return new DeliveryDestination(UUID.randomUUID(), "Matriz",
                new Address("Rua XV", "10", "Centro", "Blumenau", "SC", "89010-000", null), null);
    }
}
