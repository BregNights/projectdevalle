package br.com.senac.projectdevalle.catalog.infrastructure.web.dto;

import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.RecurrenceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record PublishOfferRequest(
        @NotBlank String productName,
        @NotNull ProductCategory category,
        @NotNull MeasurementUnit unit,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantityAvailable,
        @NotNull RecurrenceType recurrenceType,
        DayOfWeek recurrenceDayOfWeek,
        LocalDate availabilityFrom,
        LocalDate availabilityUntil,
        List<String> photoUrls
) {
}
