package br.com.senac.projectdevalle.registration.application.producer.command;

import br.com.senac.projectdevalle.registration.domain.producer.CertificationType;

import java.time.LocalDate;
import java.util.UUID;

public record AttachCertificationCommand(
        UUID producerId,
        CertificationType type,
        String proofUrl,
        LocalDate validUntil
) {
}
</content>
