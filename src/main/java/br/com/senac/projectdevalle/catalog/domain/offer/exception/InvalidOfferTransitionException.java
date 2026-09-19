package br.com.senac.projectdevalle.catalog.domain.offer.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

public class InvalidOfferTransitionException extends BusinessRuleViolationException {

    public InvalidOfferTransitionException(String message) {
        super(message);
    }
}
