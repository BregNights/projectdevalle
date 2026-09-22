package br.com.senac.projectdevalle.ordering.domain.order.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

// Operação não permitida no status atual do pedido, ou por quem não tem a vez (RF18/RF19/RF20).
public class InvalidOrderOperationException extends BusinessRuleViolationException {

    public InvalidOrderOperationException(String message) {
        super(message);
    }
}
