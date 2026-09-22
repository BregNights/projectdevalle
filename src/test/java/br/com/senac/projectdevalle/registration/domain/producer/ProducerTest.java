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

    // RN29
    @Test
    void keepsTheReasonOfSuspensionAndClearsItOnReactivation() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));
        producer.approve(address -> true);

        producer.suspend("Documento vencido");
        assertThat(producer.statusReason()).isEqualTo("Documento vencido");

        producer.reactivate(address -> true);
        assertThat(producer.status()).isEqualTo(RegistrationStatus.APPROVED);
        assertThat(producer.statusReason()).isNull();
    }

    @Test
    void rejectAndSuspendRequireAReason() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));

        assertThatThrownBy(() -> producer.reject(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThat(producer.status()).isEqualTo(RegistrationStatus.PENDING);
    }

    // RF40
    @Test
    void reactivateOnlyAllowedFromSuspended() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));
        producer.approve(address -> true);

        assertThatThrownBy(() -> producer.reactivate(address -> true))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // RF40/RN02 — reativação revalida a área de cobertura.
    @Test
    void reactivateFailsWhenOutsideCoverageArea() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));
        producer.approve(address -> true);
        producer.suspend("reason");

        assertThatThrownBy(() -> producer.reactivate(address -> false))
                .isInstanceOf(OutsideCoverageAreaException.class);
        assertThat(producer.status()).isEqualTo(RegistrationStatus.SUSPENDED);
    }

    // RF40
    @Test
    void removeIsAllowedFromAnyStatusButOnlyOnce() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));

        producer.remove("Cadastro duplicado");

        assertThat(producer.status()).isEqualTo(RegistrationStatus.REMOVED);
        assertThat(producer.isEligibleToOperate()).isFalse();
        assertThatThrownBy(() -> producer.remove("de novo")).isInstanceOf(BusinessRuleViolationException.class);
    }

    // RN02 — produtor aprovado não pode sair da área de cobertura.
    @Test
    void approvedProducerCannotMoveOriginOutsideCoverageArea() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));
        producer.approve(address -> true);
        Address outside = new Address("Rua das Flores", "1", "Centro", "Curitiba", "PR", "80020-000", null);

        assertThatThrownBy(() -> producer.updateOriginAddress(OriginLocation.withoutCoordinates(outside),
                address -> false))
                .isInstanceOf(OutsideCoverageAreaException.class);
        assertThat(producer.originLocation().address().city()).isNotEqualTo("Curitiba");
    }

    @Test
    void pendingProducerCanFixOriginAddressEvenOutsideCoverageArea() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));
        Address outside = new Address("Rua das Flores", "1", "Centro", "Curitiba", "PR", "80020-000", null);

        producer.updateOriginAddress(OriginLocation.withoutCoordinates(outside), address -> false);

        assertThat(producer.originLocation().address()).isEqualTo(outside);
    }

    @Test
    void attachCertificationRequiresValidProof() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));
        Certification expiredCertification = Certification.attach(CertificationType.ORGANIC, "https://proof",
                LocalDate.of(2020, 1, 1));

        assertThatThrownBy(() -> producer.attachCertification(expiredCertification, FIXED_CLOCK))
                .isInstanceOf(CertificationWithoutValidProofException.class);
    }

    // RF05
    @Test
    void updateOriginAddressReplacesAddressAndResetsCoordinates() {
        Producer producer = newProducerWithCoordinates(new Coordinates(-26.9, -48.6));
        Address newAddress = new Address("Rua Nova", "50", "Bairro Novo", "Itajai", "SC", "88300-000", null);

        producer.updateOriginAddress(OriginLocation.withoutCoordinates(newAddress), address -> true);

        assertThat(producer.originLocation().address()).isEqualTo(newAddress);
        assertThat(producer.isGeocodingPending()).isTrue();
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
