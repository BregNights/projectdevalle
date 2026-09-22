package br.com.senac.projectdevalle.ordering.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// RF20 — cancelamento sempre com motivo.
public record CancelOrderRequest(@NotBlank @Size(max = 1000) String reason) {
}
