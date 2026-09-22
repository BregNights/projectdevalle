package br.com.senac.projectdevalle.ordering.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public record RecurringOrderRequest(
        @NotNull UUID deliveryAddressId,
        @NotNull DayOfWeek deliveryDay,
        @Size(max = 1000) String notes,
        @NotEmpty List<@Valid Item> items
) {

    public record Item(@NotNull UUID offerId, @NotNull @DecimalMin(value = "0.001") BigDecimal quantity) {
    }
}
