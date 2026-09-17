package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.producer.CertificationType;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

public record ProducerResponse(
        UUID id,
        String name,
        ProductionType productionType,
        RegistrationStatus status,
        boolean geocodingPending,
        List<CertificationType> visibleCertifications
) {

    public static ProducerResponse from(Producer producer, Clock clock) {
        List<CertificationType> visibleCertifications = producer.visibleCertifications(clock).stream()
                .map(certification -> certification.type())
                .toList();
        return new ProducerResponse(producer.id(), producer.name(), producer.productionType(), producer.status(),
                producer.isGeocodingPending(), visibleCertifications);
    }
}
</content>
