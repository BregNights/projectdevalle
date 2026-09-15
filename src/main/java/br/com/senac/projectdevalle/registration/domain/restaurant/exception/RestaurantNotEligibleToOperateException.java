package br.com.senac.projectdevalle.registration.domain.restaurant.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

public class RestaurantNotEligibleToOperateException extends BusinessRuleViolationException {

    public RestaurantNotEligibleToOperateException(String message) {
        super(message);
    }
}
</content>
