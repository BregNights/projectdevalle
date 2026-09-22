package br.com.senac.projectdevalle.ordering.infrastructure.web.dto;

import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrder;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrderStatus;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public record RecurringOrderResponse(
        UUID id,
        UUID deliveryAddressId,
        DayOfWeek deliveryDay,
        String notes,
        RecurringOrderStatus status,
        LocalDate nextDeliveryDate,
        LocalDate nextRunDate,
        boolean suspendAfterNextRun,
        Instant lastRunAt,
        String lastRunSummary,
        List<Item> items
) {

    public record Item(UUID offerId, String productName, BigDecimal quantity) {
    }

    public static RecurringOrderResponse from(RecurringOrder order, Function<UUID, String> names) {
        return new RecurringOrderResponse(order.id(), order.deliveryAddressId(), order.deliveryDay(), order.notes(),
                order.status(), order.nextDeliveryDate(), order.nextRunDate(), order.suspendAfterNextRun(),
                order.lastRunAt(), order.lastRunSummary(),
                order.items().stream()
                        .map(item -> new Item(item.offerId(), names.apply(item.offerId()), item.quantity()))
                        .toList());
    }
}
