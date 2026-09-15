package br.com.senac.projectdevalle.registration.domain.producer.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

public class CertificationWithoutValidProofException extends BusinessRuleViolationException {

    public CertificationWithoutValidProofException() {
        super("Certification requires a proof document and a future validity date");
    }
}
</content>
