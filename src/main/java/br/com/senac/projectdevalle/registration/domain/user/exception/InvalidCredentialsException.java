package br.com.senac.projectdevalle.registration.domain.user.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

public class InvalidCredentialsException extends BusinessRuleViolationException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
</content>
