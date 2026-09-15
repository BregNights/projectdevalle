package br.com.senac.projectdevalle.registration.domain.producer.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

public class ProducerNotEligibleToOperateException extends BusinessRuleViolationException {

    public ProducerNotEligibleToOperateException(String message) {
        super(message);
    }
}
</content>
