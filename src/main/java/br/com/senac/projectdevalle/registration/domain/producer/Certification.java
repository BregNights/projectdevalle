package br.com.senac.projectdevalle.registration.domain.producer;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

public record Certification(UUID id, CertificationType type, String proofUrl, LocalDate validUntil) {

    public static Certification attach(CertificationType type, String proofUrl, LocalDate validUntil) {
        return new Certification(UUID.randomUUID(), type, proofUrl, validUntil);
    }

    // RN03 — só é considerada válida (e, portanto, visível no perfil) com comprovante e dentro da validade.
    public boolean isValid(Clock clock) {
        return proofUrl != null && !proofUrl.isBlank()
                && validUntil != null && !validUntil.isBefore(LocalDate.now(clock));
    }
}
</content>
