package br.com.senac.projectdevalle.registration.domain.producer.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

public class OutsideCoverageAreaException extends BusinessRuleViolationException {

    public OutsideCoverageAreaException() {
        super("Producer origin location is outside the platform coverage area");
    }
}
</content>
