package br.com.senac.projectdevalle.catalog.domain.offer;

import java.time.LocalDate;

// RF09/RN06 — janela de disponibilidade da oferta; obrigatória (com "until") para categorias perecíveis.
public record AvailabilityWindow(LocalDate from, LocalDate until) {

    public AvailabilityWindow {
        if (from != null && until != null && until.isBefore(from)) {
            throw new IllegalArgumentException("until must not be before from");
        }
    }
}
