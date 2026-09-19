package br.com.senac.projectdevalle.catalog.domain.offer.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

public class MissingAvailabilityDeadlineException extends BusinessRuleViolationException {

    public MissingAvailabilityDeadlineException(String message) {
        super(message);
    }
}
