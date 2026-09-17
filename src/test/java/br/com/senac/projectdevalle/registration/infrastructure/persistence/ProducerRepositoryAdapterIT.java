package br.com.senac.projectdevalle.registration.infrastructure.persistence;

import br.com.senac.projectdevalle.registration.domain.producer.Certification;
import br.com.senac.projectdevalle.registration.domain.producer.CertificationType;
import br.com.senac.projectdevalle.registration.domain.producer.OriginLocation;
import br.com.senac.projectdevalle.registration.domain.producer.Producer;
import br.com.senac.projectdevalle.registration.domain.producer.ProducerRepository;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocument;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType;
import br.com.senac.projectdevalle.support.AbstractIntegrationTest;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import br.com.senac.projectdevalle.shared.domain.vo.Cpf;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProducerRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private ProducerRepository producerRepository;

    @Test
    void savesAndReloadsProducerWithChildCollections() {
        Producer producer = Producer.register(UUID.randomUUID(), "Joao Pescador", new Cpf("12345678909"),
                ProductionType.FISHING, validOriginLocation(), List.of(validSupportingDocument()));
        producer.attachCertification(
                Certification.attach(CertificationType.GOOD_FISHING_PRACTICES, "https://proof", LocalDate.of(2030, 1, 1)),
                Clock.systemUTC());

        producerRepository.save(producer);

        Optional<Producer> reloaded = producerRepository.findById(producer.id());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().name()).isEqualTo("Joao Pescador");
        assertThat(reloaded.get().supportingDocuments()).hasSize(1);
        assertThat(reloaded.get().certifications()).hasSize(1);
        assertThat(reloaded.get().isGeocodingPending()).isFalse();
    }

    @Test
    void persistsGeocodingPendingWhenCoordinatesAreAbsent() {
        Address address = new Address("Rua Sem Nome", "1", "Centro", "Blumenau", "SC", "89010-000", null);
        Producer producer = Producer.register(UUID.randomUUID(), "Ana", new Cpf("12345678909"),
                ProductionType.FARMING, OriginLocation.withoutCoordinates(address), List.of(validSupportingDocument()));

        producerRepository.save(producer);

        Optional<Producer> reloaded = producerRepository.findById(producer.id());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().isGeocodingPending()).isTrue();
    }

    private static OriginLocation validOriginLocation() {
        Address address = new Address("Rua das Flores", "100", "Centro", "Itajai", "SC", "88301-000", null);
        return new OriginLocation(address, new Coordinates(-26.9077, -48.6613));
    }

    private static SupportingDocument validSupportingDocument() {
        return SupportingDocument.issue(SupportingDocumentType.FISHING_LICENSE, "REG-12345",
                "https://files/license.pdf");
    }
}
