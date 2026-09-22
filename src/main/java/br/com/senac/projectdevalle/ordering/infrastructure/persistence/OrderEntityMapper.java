package br.com.senac.projectdevalle.ordering.infrastructure.persistence;

import br.com.senac.projectdevalle.ordering.domain.order.Cancellation;
import br.com.senac.projectdevalle.ordering.domain.order.DeliveryDestination;
import br.com.senac.projectdevalle.ordering.domain.order.ItemTerms;
import br.com.senac.projectdevalle.ordering.domain.order.NegotiationRound;
import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderItem;
import br.com.senac.projectdevalle.ordering.domain.order.StatusChange;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Mapeamento manual (agregado com construtor privado). Rodadas de negociação, termos e mudanças de status não têm
// id no domínio: o id da linha é derivado de forma determinística (pedido + posição), para que salvar de novo o
// mesmo pedido atualize as linhas existentes em vez de apagar e recriar o histórico.
@Component
class OrderEntityMapper {

    OrderJpaEntity toEntity(Order order) {
        DeliveryDestination destination = order.destination();
        Address address = destination.address();
        Coordinates coordinates = destination.coordinates();
        Cancellation cancellation = order.cancellation();

        OrderJpaEntity entity = OrderJpaEntity.builder()
                .id(order.id())
                .checkoutId(order.checkoutId())
                .restaurantId(order.restaurantId())
                .producerId(order.producerId())
                .deliveryAddressId(destination.addressId())
                .deliveryLabel(destination.label())
                .deliveryStreet(address.street())
                .deliveryNumber(address.number())
                .deliveryNeighborhood(address.neighborhood())
                .deliveryCity(address.city())
                .deliveryState(address.state())
                .deliveryZipCode(address.zipCode())
                .deliveryComplement(address.complement())
                .deliveryLatitude(coordinates != null ? coordinates.latitude() : null)
                .deliveryLongitude(coordinates != null ? coordinates.longitude() : null)
                .requestedDeliveryDate(order.requestedDeliveryDate())
                .notes(order.notes())
                .status(order.status())
                .awaitingResponseFrom(order.awaitingResponseFrom())
                .totalAmount(order.total())
                .placedAt(order.placedAt())
                .confirmedAt(order.confirmedAt())
                .preparationStartedAt(order.preparationStartedAt())
                .pickedUpAt(order.pickedUpAt())
                .deliveredAt(order.deliveredAt())
                .deliveryAutoConfirmed(order.deliveryAutoConfirmed())
                .cancelledBy(cancellation != null ? cancellation.cancelledBy() : null)
                .cancellationReason(cancellation != null ? cancellation.reason() : null)
                .cancelledAt(cancellation != null ? cancellation.cancelledAt() : null)
                .cancelledAfterConfirmation(cancellation != null ? cancellation.afterConfirmation() : null)
                .penaltyAmount(cancellation != null ? cancellation.penaltyAmount() : null)
                .recurringOrderId(order.recurringOrderId())
                .build();

        List<OrderItem> items = order.items();
        for (int position = 0; position < items.size(); position++) {
            OrderItem item = items.get(position);
            entity.getItems().add(OrderItemJpaEntity.builder()
                    .id(item.id())
                    .order(entity)
                    .position(position)
                    .offerId(item.offerId())
                    .productName(item.productName())
                    .category(item.category())
                    .unit(item.unit())
                    .listUnitPrice(item.listUnitPrice())
                    .quantity(item.quantity())
                    .unitPrice(item.unitPrice())
                    .harvestDate(item.harvestDate())
                    .lot(item.lot())
                    .build());
        }

        for (NegotiationRound round : order.negotiation()) {
            UUID roundId = derivedId(order.id(), "round", round.number());
            OrderNegotiationRoundJpaEntity roundEntity = OrderNegotiationRoundJpaEntity.builder()
                    .id(roundId)
                    .order(entity)
                    .roundNumber(round.number())
                    .proposedBy(round.proposedBy())
                    .proposedAt(round.proposedAt())
                    .message(round.message())
                    .deliveryDate(round.deliveryDate())
                    .build();
            for (ItemTerms terms : round.terms()) {
                roundEntity.getTerms().add(OrderNegotiationTermJpaEntity.builder()
                        .id(derivedId(roundId, "term", terms.itemId()))
                        .round(roundEntity)
                        .itemId(terms.itemId())
                        .quantity(terms.quantity())
                        .unitPrice(terms.unitPrice())
                        .build());
            }
            entity.getNegotiationRounds().add(roundEntity);
        }

        List<StatusChange> history = order.history();
        for (int position = 0; position < history.size(); position++) {
            StatusChange change = history.get(position);
            entity.getStatusChanges().add(OrderStatusChangeJpaEntity.builder()
                    .id(derivedId(order.id(), "status", position))
                    .order(entity)
                    .position(position)
                    .status(change.status())
                    .actor(change.actor())
                    .occurredAt(change.occurredAt())
                    .note(change.note())
                    .build());
        }
        return entity;
    }

    Order toDomain(OrderJpaEntity entity) {
        Address address = new Address(entity.getDeliveryStreet(), entity.getDeliveryNumber(),
                entity.getDeliveryNeighborhood(), entity.getDeliveryCity(), entity.getDeliveryState(),
                entity.getDeliveryZipCode(), entity.getDeliveryComplement());
        Coordinates coordinates = entity.getDeliveryLatitude() != null && entity.getDeliveryLongitude() != null
                ? new Coordinates(entity.getDeliveryLatitude(), entity.getDeliveryLongitude())
                : null;
        DeliveryDestination destination = new DeliveryDestination(entity.getDeliveryAddressId(),
                entity.getDeliveryLabel(), address, coordinates);

        List<OrderItem> items = entity.getItems().stream()
                .map(item -> OrderItem.reconstitute(item.getId(), item.getOfferId(), item.getProductName(),
                        item.getCategory(), item.getUnit(), item.getListUnitPrice(), item.getQuantity(),
                        item.getUnitPrice(), item.getHarvestDate(), item.getLot()))
                .toList();

        List<NegotiationRound> rounds = new ArrayList<>();
        for (OrderNegotiationRoundJpaEntity round : entity.getNegotiationRounds()) {
            List<ItemTerms> terms = round.getTerms().stream()
                    .map(term -> new ItemTerms(term.getItemId(), term.getQuantity(), term.getUnitPrice()))
                    .toList();
            rounds.add(new NegotiationRound(round.getRoundNumber(), round.getProposedBy(), round.getProposedAt(),
                    round.getMessage(), round.getDeliveryDate(), terms));
        }

        List<StatusChange> history = entity.getStatusChanges().stream()
                .map(change -> new StatusChange(change.getStatus(), change.getActor(), change.getOccurredAt(),
                        change.getNote()))
                .toList();

        Cancellation cancellation = entity.getCancelledBy() != null
                ? new Cancellation(entity.getCancelledBy(), entity.getCancellationReason(), entity.getCancelledAt(),
                        Boolean.TRUE.equals(entity.getCancelledAfterConfirmation()), entity.getPenaltyAmount())
                : null;

        return Order.reconstitute(entity.getId(), entity.getCheckoutId(), entity.getRestaurantId(),
                entity.getProducerId(), destination, entity.getRequestedDeliveryDate(), entity.getNotes(), items,
                entity.getStatus(), entity.getAwaitingResponseFrom(), rounds, history, entity.getPlacedAt(),
                entity.getConfirmedAt(), entity.getPreparationStartedAt(), entity.getPickedUpAt(),
                entity.getDeliveredAt(), entity.isDeliveryAutoConfirmed(), cancellation,
                entity.getRecurringOrderId());
    }

    private static UUID derivedId(UUID parent, String kind, Object key) {
        return UUID.nameUUIDFromBytes((parent + ":" + kind + ":" + key).getBytes(StandardCharsets.UTF_8));
    }
}
