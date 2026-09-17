package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocument;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SupportingDocumentRequest(
        @NotNull SupportingDocumentType type,
        @NotBlank String documentNumber,
        @NotBlank String fileUrl
) {

    public SupportingDocument toDomain() {
        return SupportingDocument.issue(type, documentNumber, fileUrl);
    }
}
</content>
