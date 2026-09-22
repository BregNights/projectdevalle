package br.com.senac.projectdevalle.ordering.domain.order;

import java.math.BigDecimal;
import java.util.UUID;

// RF18 — condições propostas para um item numa rodada de negociação.
public record ItemTerms(UUID itemId, BigDecimal quantity, BigDecimal unitPrice) {

    public ItemTerms {
        if (itemId == null) {
            throw new IllegalArgumentException("itemId is required");
        }
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("unitPrice must be greater than zero");
        }
    }
}
