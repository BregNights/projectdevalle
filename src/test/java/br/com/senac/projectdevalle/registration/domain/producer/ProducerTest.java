package br.com.senac.projectdevalle.registration.domain.producer;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.producer.exception.CertificationWithoutValidProofException;
import br.com.senac.projectdevalle.registration.domain.producer.exception.OutsideCoverageAreaException;
import br.com.senac.projectdevalle.registration.domain.producer.exception.ProducerNotEligibleToOperateException;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import br.com.senac.projectdevalle.shared.domain.vo.Cpf;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProducerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void registersAsPendingWithGeocodingResolved() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));

        assertThat(producer.status()).isEqualTo(RegistrationStatus.PENDING);
        assertThat(producer.isGeocodingPending()).isFalse();
        assertThat(producer.isEligibleToOperate()).isFalse();
    }

    @Test
    void requiresAtLeastOneSupportingDocument() {
        assertThatThrownBy(() -> Producer.register(UUID.randomUUID(), "Joao", validCpf(), ProductionType.FARMING,
                validOriginLocation(new Coordinates(-26.9, -48.6)), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void approveSucceedsWhenWithinCoverageAreaAndGeocoded() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));

        producer.approve(address -> true);

        assertThat(producer.isEligibleToOperate()).isTrue();
    }

    @Test
    void approveFailsWhenOutsideCoverageArea() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));

        assertThatThrownBy(() -> producer.approve(address -> false))
                .isInstanceOf(OutsideCoverageAreaException.class);
        assertThat(producer.isEligibleToOperate()).isFalse();
    }

    @Test
    void approveFailsWhenGeocodingIsPending() {
        Producer producer = newProducerWithCoordinates(null);

        assertThatThrownBy(() -> producer.approve(address -> true))
                .isInstanceOf(ProducerNotEligibleToOperateException.class);
    }

    @Test
    void approveFailsWhenNotPending() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));
        producer.approve(address -> true);

        assertThatThrownBy(() -> producer.approve(address -> true))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void suspendOnlyAllowedAfterApproval() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));

        assertThatThrownBy(() -> producer.suspend("reason"))
                .isInstanceOf(BusinessRuleViolationException.class);

        producer.approve(address -> true);
        producer.suspend("reason");

        assertThat(producer.isEligibleToOperate()).isFalse();
    }

    @Test
    void attachCertificationRequiresValidProof() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));
        Certification expiredCertification = Certification.attach(CertificationType.ORGANIC, "https://proof",
                LocalDate.of(2020, 1, 1));

        assertThatThrownBy(() -> producer.attachCertification(expiredCertification, FIXED_CLOCK))
                .isInstanceOf(CertificationWithoutValidProofException.class);
    }

    @Test
    void visibleCertificationsOnlyIncludeValidOnes() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));
        Certification validCertification = Certification.attach(CertificationType.ORGANIC, "https://proof",
                LocalDate.of(2026, 12, 31));

        producer.attachCertification(validCertification, FIXED_CLOCK);

        assertThat(producer.visibleCertifications(FIXED_CLOCK)).hasSize(1);
    }

    private static Producer newProducerWithCoordinates(Coordinates coordinates) {
        return Producer.register(UUID.randomUUID(), "Joao", validCpf(), ProductionType.FARMING,
                validOriginLocation(coordinates), List.of(validSupportingDocument()));
    }

    private static Cpf validCpf() {
        return new Cpf("12345678909");
    }

    private static OriginLocation validOriginLocation(Coordinates coordinates) {
        Address address = new Address("Rua das Flores", "100", "Centro", "Blumenau", "SC", "89010-000", null);
        return coordinates == null ? OriginLocation.withoutCoordinates(address) : new OriginLocation(address, coordinates);
    }

    private static SupportingDocument validSupportingDocument() {
        return SupportingDocument.issue(SupportingDocumentType.CPF, "12345678909", "https://files/doc.pdf");
    }
}
</content>
