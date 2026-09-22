package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.producer.CertificationType;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProducerResponse(
        UUID id,
        String name,
        ProductionType productionType,
        RegistrationStatus status,
        String statusReason,
        boolean geocodingPending,
        List<CertificationType> visibleCertifications,
        List<SupportingDocumentView> supportingDocuments,
        List<CertificationView> certifications
) {

    // RF03 — documentos que o administrador confere antes de aprovar.
    public record SupportingDocumentView(SupportingDocumentType type, String documentNumber, String fileUrl) {
    }

    // RN03 — todas as certificações (inclusive expiradas), com o comprovante, para conferência.
    public record CertificationView(CertificationType type, String proofUrl, LocalDate validUntil, boolean valid) {
    }

    // Visão do administrador e do próprio produtor: inclui motivo do status (RN29), documentos e comprovantes.
    public static ProducerResponse from(Producer producer, Clock clock) {
        List<SupportingDocumentView> documents = producer.supportingDocuments().stream()
                .map(document -> new SupportingDocumentView(document.type(), document.documentNumber(),
                        document.fileUrl()))
                .toList();
        List<CertificationView> certifications = producer.certifications().stream()
                .map(certification -> new CertificationView(certification.type(), certification.proofUrl(),
                        certification.validUntil(), certification.isValid(clock)))
                .toList();
        return build(producer, clock, producer.statusReason(), documents, certifications);
    }

    // Visão de terceiros (RN37/RNF06): sem motivo do status, sem documentos pessoais nem comprovantes.
    public static ProducerResponse publicView(Producer producer, Clock clock) {
        return build(producer, clock, null, List.of(), List.of());
    }

    private static ProducerResponse build(Producer producer, Clock clock, String statusReason,
                                          List<SupportingDocumentView> documents,
                                          List<CertificationView> certifications) {
        List<CertificationType> visibleCertifications = producer.visibleCertifications(clock).stream()
                .map(certification -> certification.type())
                .toList();
        return new ProducerResponse(producer.id(), producer.name(), producer.productionType(), producer.status(),
                statusReason, producer.isGeocodingPending(), visibleCertifications, documents, certifications);
    }
}
