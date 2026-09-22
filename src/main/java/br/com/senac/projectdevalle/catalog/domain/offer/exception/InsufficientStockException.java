package br.com.senac.projectdevalle.catalog.domain.offer.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

import java.math.BigDecimal;

public class InsufficientStockException extends BusinessRuleViolationException {

    public InsufficientStockException(String productName, BigDecimal available, BigDecimal requested) {
        super("Insufficient stock for " + productName + ": available " + available.stripTrailingZeros().toPlainString()
                + ", requested " + requested.stripTrailingZeros().toPlainString());
    }
}
