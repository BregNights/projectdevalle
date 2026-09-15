package br.com.senac.projectdevalle.registration.domain.producer;

import java.util.UUID;

public record SupportingDocument(UUID id, SupportingDocumentType type, String documentNumber, String fileUrl) {

    public static SupportingDocument issue(SupportingDocumentType type, String documentNumber, String fileUrl) {
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new IllegalArgumentException("documentNumber must not be blank");
        }
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("fileUrl must not be blank");
        }
        return new SupportingDocument(UUID.randomUUID(), type, documentNumber, fileUrl);
    }
}
</content>
