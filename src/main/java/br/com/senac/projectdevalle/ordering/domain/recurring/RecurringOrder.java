package br.com.senac.projectdevalle.ordering.domain.recurring;

import br.com.senac.projectdevalle.ordering.domain.order.exception.InvalidOrderOperationException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// RF21 — pedido recorrente semanal: nos dias certos, o sistema gera os pedidos sozinho (um por produtor),
// sem o restaurante precisar recriá-los. Os pedidos gerados seguem o fluxo normal (aceite do produtor etc.).
public class RecurringOrder {

    // Os pedidos de cada semana são gerados com esta antecedência em relação à entrega, para dar tempo ao
    // produtor de aceitar/negociar e preparar.
    public static final int GENERATION_LEAD_DAYS = 2;

    private final UUID id;
    private final UUID restaurantId;
    private final UUID deliveryAddressId;
    private final DayOfWeek deliveryDay;
    private final List<RecurringOrderItem> items;
    private final String notes;
    private RecurringOrderStatus status;
    private LocalDate nextDeliveryDate;
    private boolean suspendAfterNextRun;
    private Instant lastRunAt;
    private String lastRunSummary;
    private final Instant createdAt;

    private RecurringOrder(UUID id, UUID restaurantId, UUID deliveryAddressId, DayOfWeek deliveryDay,
                           List<RecurringOrderItem> items, String notes, RecurringOrderStatus status,
                           LocalDate nextDeliveryDate, Instant createdAt) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.deliveryAddressId = deliveryAddressId;
        this.deliveryDay = deliveryDay;
        this.items = List.copyOf(items);
        this.notes = notes;
        this.status = status;
        this.nextDeliveryDate = nextDeliveryDate;
        this.createdAt = createdAt;
    }

    public static RecurringOrder create(UUID restaurantId, UUID deliveryAddressId, DayOfWeek deliveryDay,
                                        List<RecurringOrderItem> items, String notes, Clock clock) {
        if (restaurantId == null || deliveryAddressId == null || deliveryDay == null) {
            throw new IllegalArgumentException("restaurant, delivery address and delivery day are required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("a recurring order needs at least one item");
        }
        Set<UUID> offers = new HashSet<>();
        for (RecurringOrderItem item : items) {
            if (!offers.add(item.offerId())) {
                throw new IllegalArgumentException("each offer can appear only once");
            }
        }
        return new RecurringOrder(UUID.randomUUID(), restaurantId, deliveryAddressId, deliveryDay, items,
                notes == null || notes.isBlank() ? null : notes.trim(), RecurringOrderStatus.ACTIVE,
                firstDeliveryOnOrAfter(LocalDate.now(clock).plusDays(GENERATION_LEAD_DAYS), deliveryDay),
                Instant.now(clock));
    }

    public static RecurringOrder reconstitute(UUID id, UUID restaurantId, UUID deliveryAddressId,
                                              DayOfWeek deliveryDay, List<RecurringOrderItem> items, String notes,
                                              RecurringOrderStatus status, LocalDate nextDeliveryDate,
                                              boolean suspendAfterNextRun, Instant lastRunAt, String lastRunSummary,
                                              Instant createdAt) {
        RecurringOrder order = new RecurringOrder(id, restaurantId, deliveryAddressId, deliveryDay, items, notes,
                status, nextDeliveryDate, createdAt);
        order.suspendAfterNextRun = suspendAfterNextRun;
        order.lastRunAt = lastRunAt;
        order.lastRunSummary = lastRunSummary;
        return order;
    }

    // Data em que os pedidos da próxima entrega são gerados.
    public LocalDate nextRunDate() {
        return nextDeliveryDate.minusDays(GENERATION_LEAD_DAYS);
    }

    public boolean isDue(Clock clock) {
        return status == RecurringOrderStatus.ACTIVE && !LocalDate.now(clock).isBefore(nextRunDate());
    }

    // Após gerar os pedidos da semana: avança para a próxima entrega e aplica uma suspensão agendada (RN12).
    public void markExecuted(String summary, Clock clock) {
        this.lastRunAt = Instant.now(clock);
        this.lastRunSummary = summary;
        LocalDate today = LocalDate.now(clock);
        do {
            nextDeliveryDate = nextDeliveryDate.plusWeeks(1);
        } while (nextRunDate().isBefore(today));
        if (suspendAfterNextRun) {
            this.status = RecurringOrderStatus.SUSPENDED;
            this.suspendAfterNextRun = false;
        }
    }

    // RN12 — suspensão pelo restaurante com aviso mínimo antes da próxima execução. Com aviso suficiente,
    // suspende já; dentro do prazo mínimo, a próxima execução ainda acontece e a suspensão vale logo depois.
    // Retorna true se a suspensão foi imediata.
    public boolean suspend(Duration minimumNotice, Clock clock) {
        if (status != RecurringOrderStatus.ACTIVE) {
            throw new InvalidOrderOperationException("Only active recurring orders can be suspended");
        }
        Instant nextRun = nextRunDate().atStartOfDay(clock.getZone()).toInstant();
        if (!Instant.now(clock).plus(minimumNotice).isAfter(nextRun)) {
            this.status = RecurringOrderStatus.SUSPENDED;
            this.suspendAfterNextRun = false;
            return true;
        }
        this.suspendAfterNextRun = true;
        return false;
    }

    public void resume(Clock clock) {
        if (status == RecurringOrderStatus.ACTIVE && suspendAfterNextRun) {
            this.suspendAfterNextRun = false;
            return;
        }
        if (status != RecurringOrderStatus.SUSPENDED) {
            throw new InvalidOrderOperationException("Only suspended recurring orders can be resumed");
        }
        this.status = RecurringOrderStatus.ACTIVE;
        this.nextDeliveryDate = firstDeliveryOnOrAfter(LocalDate.now(clock).plusDays(GENERATION_LEAD_DAYS),
                deliveryDay);
    }

    private static LocalDate firstDeliveryOnOrAfter(LocalDate date, DayOfWeek day) {
        return date.with(TemporalAdjusters.nextOrSame(day));
    }

    public UUID id() {
        return id;
    }

    public UUID restaurantId() {
        return restaurantId;
    }

    public UUID deliveryAddressId() {
        return deliveryAddressId;
    }

    public DayOfWeek deliveryDay() {
        return deliveryDay;
    }

    public List<RecurringOrderItem> items() {
        return items;
    }

    public String notes() {
        return notes;
    }

    public RecurringOrderStatus status() {
        return status;
    }

    public LocalDate nextDeliveryDate() {
        return nextDeliveryDate;
    }

    public boolean suspendAfterNextRun() {
        return suspendAfterNextRun;
    }

    public Instant lastRunAt() {
        return lastRunAt;
    }

    public String lastRunSummary() {
        return lastRunSummary;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public record RecurringOrderItem(UUID offerId, BigDecimal quantity) {

        public RecurringOrderItem {
            if (offerId == null) {
                throw new IllegalArgumentException("offerId is required");
            }
            if (quantity == null || quantity.signum() <= 0) {
                throw new IllegalArgumentException("quantity must be greater than zero");
            }
        }
    }
}
