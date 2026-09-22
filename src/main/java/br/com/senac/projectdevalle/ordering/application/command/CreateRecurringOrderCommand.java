package br.com.senac.projectdevalle.ordering.application.command;

import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrder.RecurringOrderItem;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public record CreateRecurringOrderCommand(UUID userId, UUID deliveryAddressId, DayOfWeek deliveryDay, String notes,
                                          List<RecurringOrderItem> items) {
}
