package br.com.senac.projectdevalle.catalog.infrastructure.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateOfferQuantityRequest(@NotNull @DecimalMin(value = "0") BigDecimal quantityAvailable) {
}
