package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.producer.CertificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AttachCertificationRequest(
        @NotNull CertificationType type,
        @NotBlank String proofUrl,
        @NotNull LocalDate validUntil
) {
}
</content>
