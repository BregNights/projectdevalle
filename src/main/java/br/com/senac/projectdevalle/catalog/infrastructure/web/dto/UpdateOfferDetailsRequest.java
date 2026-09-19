package br.com.senac.projectdevalle.catalog.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UpdateOfferDetailsRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        LocalDate availabilityFrom,
        LocalDate availabilityUntil,
        List<String> photoUrls
) {
}
