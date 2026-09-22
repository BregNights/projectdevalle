package br.com.senac.projectdevalle.ordering.domain.order;

import java.time.LocalDate;
import java.util.UUID;

// RN23 — rastreabilidade informada pelo produtor ao despachar: data de colheita/captura e lote (quando houver).
public record Traceability(UUID itemId, LocalDate harvestDate, String lot) {
}
