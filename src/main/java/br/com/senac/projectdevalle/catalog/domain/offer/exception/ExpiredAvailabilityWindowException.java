package br.com.senac.projectdevalle.catalog.domain.offer.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

public class ExpiredAvailabilityWindowException extends BusinessRuleViolationException {

    public ExpiredAvailabilityWindowException() {
        super("Offer availability deadline is already in the past");
    }
}
