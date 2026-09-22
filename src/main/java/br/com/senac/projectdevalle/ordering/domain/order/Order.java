package br.com.senac.projectdevalle.ordering.domain.order;

import br.com.senac.projectdevalle.ordering.domain.order.exception.InvalidOrderOperationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Pedido de um restaurante a UM produtor. Um carrinho com vários produtores (RF16) gera um pedido por produtor,
// agrupados pelo mesmo checkoutId — cada produtor aceita, negocia e entrega o seu independentemente.
public class Order {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final UUID id;
    private final UUID checkoutId;
    private final UUID restaurantId;
    private final UUID producerId;
    private final DeliveryDestination destination;
    private LocalDate requestedDeliveryDate;
    private final String notes;
    private final List<OrderItem> items;
    private OrderStatus status;
    private OrderParty awaitingResponseFrom;
    private final List<NegotiationRound> negotiation;
    private final List<StatusChange> history;
    private final Instant placedAt;
    private Instant confirmedAt;
    private Instant preparationStartedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private boolean deliveryAutoConfirmed;
    private Cancellation cancellation;
    private final UUID recurringOrderId;

    private Order(UUID id, UUID checkoutId, UUID restaurantId, UUID producerId, DeliveryDestination destination,
                  LocalDate requestedDeliveryDate, String notes, List<OrderItem> items, OrderStatus status,
                  OrderParty awaitingResponseFrom, List<NegotiationRound> negotiation, List<StatusChange> history,
                  Instant placedAt, UUID recurringOrderId) {
        this.id = id;
        this.checkoutId = checkoutId;
        this.restaurantId = restaurantId;
        this.producerId = producerId;
        this.destination = destination;
        this.requestedDeliveryDate = requestedDeliveryDate;
        this.notes = notes;
        this.items = new ArrayList<>(items);
        this.status = status;
        this.awaitingResponseFrom = awaitingResponseFrom;
        this.negotiation = new ArrayList<>(negotiation);
        this.history = new ArrayList<>(history);
        this.placedAt = placedAt;
        this.recurringOrderId = recurringOrderId;
    }

    // RF16/RF18 — o restaurante faz o pedido; os termos iniciais (com ou sem preço proposto) são a 1ª rodada
    // da negociação e o pedido fica aguardando a resposta do produtor.
    public static Order place(UUID checkoutId, UUID restaurantId, UUID producerId, DeliveryDestination destination,
                              LocalDate requestedDeliveryDate, String notes, List<NewOrderItem> newItems,
                              UUID recurringOrderId, Clock clock) {
        if (restaurantId == null || producerId == null || destination == null) {
            throw new IllegalArgumentException("restaurant, producer and destination are required");
        }
        requireDeliveryDateNotInPast(requestedDeliveryDate, clock);
        if (newItems == null || newItems.isEmpty()) {
            throw new IllegalArgumentException("an order needs at least one item");
        }
        Set<UUID> offers = new HashSet<>();
        List<OrderItem> items = new ArrayList<>();
        for (NewOrderItem newItem : newItems) {
            if (!offers.add(newItem.offerId())) {
                throw new IllegalArgumentException("each offer can appear only once per order");
            }
            items.add(OrderItem.from(newItem));
        }
        Instant now = Instant.now(clock);
        NegotiationRound opening = new NegotiationRound(1, OrderParty.RESTAURANT, now, null, requestedDeliveryDate,
                items.stream().map(item -> new ItemTerms(item.id(), item.quantity(), item.unitPrice())).toList());
        StatusChange placed = new StatusChange(OrderStatus.PENDING, OrderParty.RESTAURANT, now, "Pedido realizado");
        return new Order(UUID.randomUUID(), checkoutId, restaurantId, producerId, destination, requestedDeliveryDate,
                blankToNull(notes), items, OrderStatus.PENDING, OrderParty.PRODUCER, List.of(opening),
                List.of(placed), now, recurringOrderId);
    }

    public static Order reconstitute(UUID id, UUID checkoutId, UUID restaurantId, UUID producerId,
                                     DeliveryDestination destination, LocalDate requestedDeliveryDate, String notes,
                                     List<OrderItem> items, OrderStatus status, OrderParty awaitingResponseFrom,
                                     List<NegotiationRound> negotiation, List<StatusChange> history, Instant placedAt,
                                     Instant confirmedAt, Instant preparationStartedAt, Instant pickedUpAt,
                                     Instant deliveredAt, boolean deliveryAutoConfirmed, Cancellation cancellation,
                                     UUID recurringOrderId) {
        Order order = new Order(id, checkoutId, restaurantId, producerId, destination, requestedDeliveryDate, notes,
                items, status, awaitingResponseFrom, negotiation, history, placedAt, recurringOrderId);
        order.confirmedAt = confirmedAt;
        order.preparationStartedAt = preparationStartedAt;
        order.pickedUpAt = pickedUpAt;
        order.deliveredAt = deliveredAt;
        order.deliveryAutoConfirmed = deliveryAutoConfirmed;
        order.cancellation = cancellation;
        return order;
    }

    // RF18/RN09 — quem tem a vez aceita os termos vigentes: o pedido é confirmado (a baixa de estoque é feita
    // pela camada de aplicação, na mesma transação). Enquanto não há pagamento integrado, o aceite confirma.
    public void accept(OrderParty party, Clock clock) {
        requireTurn(party, "accept");
        Instant now = Instant.now(clock);
        this.status = OrderStatus.CONFIRMED;
        this.awaitingResponseFrom = null;
        this.confirmedAt = now;
        history.add(new StatusChange(OrderStatus.CONFIRMED, party, now, "Termos aceitos"));
    }

    // RF18 — contraproposta de quantidade/preço (e, opcionalmente, nova data de entrega). A vez passa à outra parte.
    public void counterPropose(OrderParty party, List<ItemTerms> terms, LocalDate newDeliveryDate, String message,
                               Clock clock) {
        requireTurn(party, "counter-propose");
        if (terms == null || terms.isEmpty()) {
            throw new IllegalArgumentException("a counter-proposal needs at least one item");
        }
        Map<UUID, OrderItem> byId = new HashMap<>();
        items.forEach(item -> byId.put(item.id(), item));
        Set<UUID> seen = new HashSet<>();
        for (ItemTerms itemTerms : terms) {
            if (!byId.containsKey(itemTerms.itemId())) {
                throw new IllegalArgumentException("item does not belong to this order: " + itemTerms.itemId());
            }
            if (!seen.add(itemTerms.itemId())) {
                throw new IllegalArgumentException("item repeated in the proposal: " + itemTerms.itemId());
            }
        }
        if (newDeliveryDate != null) {
            requireDeliveryDateNotInPast(newDeliveryDate, clock);
            this.requestedDeliveryDate = newDeliveryDate;
        }
        terms.forEach(itemTerms -> byId.get(itemTerms.itemId()).applyTerms(itemTerms));
        Instant now = Instant.now(clock);
        negotiation.add(new NegotiationRound(negotiation.size() + 1, party, now, blankToNull(message), newDeliveryDate,
                terms));
        this.awaitingResponseFrom = party.counterpart();
        history.add(new StatusChange(OrderStatus.PENDING, party, now, "Contraproposta enviada"));
    }

    // RF20/RN10/RN11 — cancelamento com motivo:
    // - pendente: qualquer parte recusa/desiste, sem efeitos;
    // - confirmado: sem multa para ninguém; se for o produtor, conta na taxa de cumprimento (RN11);
    // - em preparo: restaurante paga multa proporcional (RN10); produtor não paga multa, mas conta (RN11);
    // - em transporte ou entregue: não pode mais ser cancelado (vale o canal de disputa — RF34).
    // Retorna true se o pedido tinha estoque reservado, que deve voltar para a oferta.
    public boolean cancel(OrderParty party, String reason, BigDecimal penaltyPercentage, Clock clock) {
        if (party != OrderParty.RESTAURANT && party != OrderParty.PRODUCER) {
            throw new InvalidOrderOperationException("Only the restaurant or the producer can cancel an order");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("a cancellation reason is required");
        }
        if (status == OrderStatus.IN_TRANSIT || status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new InvalidOrderOperationException("Cannot cancel an order with status " + status);
        }
        boolean heldStock = status.holdsStock();
        boolean afterConfirmation = status != OrderStatus.PENDING;
        BigDecimal penalty = BigDecimal.ZERO;
        if (status == OrderStatus.IN_PREPARATION && party == OrderParty.RESTAURANT) {
            penalty = total().multiply(penaltyPercentage == null ? BigDecimal.ZERO : penaltyPercentage)
                    .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        }
        Instant now = Instant.now(clock);
        this.cancellation = new Cancellation(party, reason.trim(), now, afterConfirmation, penalty);
        this.status = OrderStatus.CANCELLED;
        this.awaitingResponseFrom = null;
        history.add(new StatusChange(OrderStatus.CANCELLED, party, now, reason.trim()));
        return heldStock;
    }

    // RF19 — o produtor informa que começou a colheita/pesca/preparo.
    public void startPreparation(OrderParty party, Clock clock) {
        requireParty(party, OrderParty.PRODUCER, "start preparation");
        requireStatus(OrderStatus.CONFIRMED, "start preparation");
        Instant now = Instant.now(clock);
        this.status = OrderStatus.IN_PREPARATION;
        this.preparationStartedAt = now;
        history.add(new StatusChange(OrderStatus.IN_PREPARATION, party, now, null));
    }

    // RF19/RF30/RN23 — saída para entrega: registra o horário de coleta e a rastreabilidade de cada item
    // (data de colheita/captura obrigatória, não futura; lote quando houver).
    public void dispatch(OrderParty party, List<Traceability> traceability, Clock clock) {
        requireParty(party, OrderParty.PRODUCER, "dispatch");
        if (status != OrderStatus.CONFIRMED && status != OrderStatus.IN_PREPARATION) {
            throw new InvalidOrderOperationException("Cannot dispatch an order with status " + status);
        }
        Map<UUID, Traceability> byItem = new HashMap<>();
        for (Traceability entry : traceability == null ? List.<Traceability>of() : traceability) {
            byItem.put(entry.itemId(), entry);
        }
        LocalDate today = LocalDate.now(clock);
        for (OrderItem item : items) {
            Traceability entry = byItem.get(item.id());
            if (entry == null || entry.harvestDate() == null) {
                throw new IllegalArgumentException("harvest/catch date is required for " + item.productName()
                        + " (RN23)");
            }
            if (entry.harvestDate().isAfter(today)) {
                throw new IllegalArgumentException("harvest/catch date cannot be in the future");
            }
        }
        items.forEach(item -> item.recordTraceability(byItem.get(item.id()).harvestDate(),
                byItem.get(item.id()).lot()));
        Instant now = Instant.now(clock);
        if (preparationStartedAt == null) {
            preparationStartedAt = now;
        }
        this.status = OrderStatus.IN_TRANSIT;
        this.pickedUpAt = now;
        history.add(new StatusChange(OrderStatus.IN_TRANSIT, party, now, null));
    }

    // RF19/RF30 — o restaurante confirma o recebimento.
    public void confirmReceipt(OrderParty party, Clock clock) {
        requireParty(party, OrderParty.RESTAURANT, "confirm receipt");
        requireStatus(OrderStatus.IN_TRANSIT, "confirm receipt");
        markDelivered(party, false, Instant.now(clock), "Recebimento confirmado");
    }

    // RN14 — sem confirmação nem disputa dentro do prazo, o recebimento é considerado confirmado.
    public boolean autoConfirmReceipt(Duration deadline, Clock clock) {
        if (status != OrderStatus.IN_TRANSIT || pickedUpAt == null) {
            return false;
        }
        Instant now = Instant.now(clock);
        if (pickedUpAt.plus(deadline).isAfter(now)) {
            return false;
        }
        markDelivered(OrderParty.SYSTEM, true, now, "Recebimento confirmado automaticamente por prazo");
        return true;
    }

    private void markDelivered(OrderParty party, boolean automatic, Instant now, String note) {
        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = now;
        this.deliveryAutoConfirmed = automatic;
        history.add(new StatusChange(OrderStatus.DELIVERED, party, now, note));
    }

    public BigDecimal total() {
        return items.stream().map(OrderItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireTurn(OrderParty party, String action) {
        requireStatus(OrderStatus.PENDING, action);
        if (party != awaitingResponseFrom) {
            throw new InvalidOrderOperationException("It is not " + party + "'s turn to " + action
                    + "; waiting for " + awaitingResponseFrom);
        }
    }

    private void requireStatus(OrderStatus expected, String action) {
        if (status != expected) {
            throw new InvalidOrderOperationException("Cannot " + action + " an order with status " + status);
        }
    }

    private static void requireParty(OrderParty actual, OrderParty expected, String action) {
        if (actual != expected) {
            throw new InvalidOrderOperationException("Only the " + expected + " can " + action);
        }
    }

    private static void requireDeliveryDateNotInPast(LocalDate date, Clock clock) {
        if (date == null) {
            throw new IllegalArgumentException("requested delivery date is required");
        }
        if (date.isBefore(LocalDate.now(clock))) {
            throw new IllegalArgumentException("requested delivery date cannot be in the past");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public UUID id() {
        return id;
    }

    public UUID checkoutId() {
        return checkoutId;
    }

    public UUID restaurantId() {
        return restaurantId;
    }

    public UUID producerId() {
        return producerId;
    }

    public DeliveryDestination destination() {
        return destination;
    }

    public LocalDate requestedDeliveryDate() {
        return requestedDeliveryDate;
    }

    public String notes() {
        return notes;
    }

    public List<OrderItem> items() {
        return List.copyOf(items);
    }

    public OrderStatus status() {
        return status;
    }

    public OrderParty awaitingResponseFrom() {
        return awaitingResponseFrom;
    }

    public List<NegotiationRound> negotiation() {
        return List.copyOf(negotiation);
    }

    public List<StatusChange> history() {
        return List.copyOf(history);
    }

    public Instant placedAt() {
        return placedAt;
    }

    public Instant confirmedAt() {
        return confirmedAt;
    }

    public Instant preparationStartedAt() {
        return preparationStartedAt;
    }

    public Instant pickedUpAt() {
        return pickedUpAt;
    }

    public Instant deliveredAt() {
        return deliveredAt;
    }

    public boolean deliveryAutoConfirmed() {
        return deliveryAutoConfirmed;
    }

    public Cancellation cancellation() {
        return cancellation;
    }

    public UUID recurringOrderId() {
        return recurringOrderId;
    }
}
