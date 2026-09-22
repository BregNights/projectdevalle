package br.com.senac.projectdevalle.ordering.infrastructure.web.dto;

import br.com.senac.projectdevalle.ordering.domain.order.Cancellation;
import br.com.senac.projectdevalle.ordering.domain.order.DeliveryDestination;
import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderItem;
import br.com.senac.projectdevalle.ordering.domain.order.OrderParty;
import br.com.senac.projectdevalle.ordering.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID checkoutId,
        UUID restaurantId,
        String restaurantName,
        UUID producerId,
        String producerName,
        Destination destination,
        LocalDate requestedDeliveryDate,
        String notes,
        OrderStatus status,
        OrderParty awaitingResponseFrom,
        OrderParty yourRole,
        boolean yourTurn,
        BigDecimal total,
        List<Item> items,
        List<Round> negotiation,
        List<Event> history,
        Instant placedAt,
        Instant confirmedAt,
        Instant preparationStartedAt,
        Instant pickedUpAt,
        Instant deliveredAt,
        boolean deliveryAutoConfirmed,
        CancellationView cancellation,
        UUID recurringOrderId,
        // RN10 — multa que o restaurante pagaria se cancelasse agora (só para o restaurante e só em preparo).
        BigDecimal cancellationPenaltyIfCancelledNow
) {

    public record Destination(String label, String street, String number, String neighborhood, String city,
                              String state, String zipCode, String complement) {
    }

    public record Item(UUID id, UUID offerId, String productName, String category, String unit,
                       BigDecimal listUnitPrice, BigDecimal quantity, BigDecimal unitPrice, BigDecimal subtotal,
                       LocalDate harvestDate, String lot) {
    }

    public record Round(int number, OrderParty proposedBy, Instant proposedAt, String message, LocalDate deliveryDate,
                        List<Terms> terms) {
    }

    public record Terms(UUID itemId, BigDecimal quantity, BigDecimal unitPrice) {
    }

    public record Event(OrderStatus status, OrderParty actor, Instant occurredAt, String note) {
    }

    public record CancellationView(OrderParty cancelledBy, String reason, Instant cancelledAt,
                                   boolean afterConfirmation, BigDecimal penaltyAmount) {
    }

    public static OrderResponse from(Order order, OrderParty viewer, String restaurantName, String producerName,
                                     BigDecimal penaltyPercentage) {
        DeliveryDestination destination = order.destination();
        Cancellation cancellation = order.cancellation();
        BigDecimal penaltyNow = viewer == OrderParty.RESTAURANT && order.status() == OrderStatus.IN_PREPARATION
                ? order.total().multiply(penaltyPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : null;
        return new OrderResponse(
                order.id(),
                order.checkoutId(),
                order.restaurantId(),
                restaurantName,
                order.producerId(),
                producerName,
                new Destination(destination.label(), destination.address().street(), destination.address().number(),
                        destination.address().neighborhood(), destination.address().city(),
                        destination.address().state(), destination.address().zipCode(),
                        destination.address().complement()),
                order.requestedDeliveryDate(),
                order.notes(),
                order.status(),
                order.awaitingResponseFrom(),
                viewer,
                order.status() == OrderStatus.PENDING && order.awaitingResponseFrom() == viewer,
                order.total(),
                order.items().stream().map(OrderResponse::toItem).toList(),
                order.negotiation().stream()
                        .map(round -> new Round(round.number(), round.proposedBy(), round.proposedAt(),
                                round.message(), round.deliveryDate(), round.terms().stream()
                                .map(terms -> new Terms(terms.itemId(), terms.quantity(), terms.unitPrice()))
                                .toList()))
                        .toList(),
                order.history().stream()
                        .map(change -> new Event(change.status(), change.actor(), change.occurredAt(), change.note()))
                        .toList(),
                order.placedAt(),
                order.confirmedAt(),
                order.preparationStartedAt(),
                order.pickedUpAt(),
                order.deliveredAt(),
                order.deliveryAutoConfirmed(),
                cancellation == null ? null : new CancellationView(cancellation.cancelledBy(), cancellation.reason(),
                        cancellation.cancelledAt(), cancellation.afterConfirmation(), cancellation.penaltyAmount()),
                order.recurringOrderId(),
                penaltyNow);
    }

    private static Item toItem(OrderItem item) {
        return new Item(item.id(), item.offerId(), item.productName(), item.category(), item.unit(),
                item.listUnitPrice(), item.quantity(), item.unitPrice(), item.subtotal(), item.harvestDate(),
                item.lot());
    }
}
