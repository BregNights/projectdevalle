package br.com.senac.projectdevalle.catalog.domain.offer.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

public class ProducerNotEligibleException extends BusinessRuleViolationException {

    public ProducerNotEligibleException(String message) {
        super(message);
    }
}
