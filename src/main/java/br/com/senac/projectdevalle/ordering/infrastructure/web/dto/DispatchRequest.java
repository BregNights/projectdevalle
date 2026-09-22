package br.com.senac.projectdevalle.ordering.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// RN23 — rastreabilidade de cada item informada ao despachar o pedido.
public record DispatchRequest(@NotEmpty List<@Valid ItemTraceability> items) {

    public record ItemTraceability(@NotNull UUID itemId, @NotNull LocalDate harvestDate, @Size(max = 80) String lot) {
    }
}
