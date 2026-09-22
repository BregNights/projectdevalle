package br.com.senac.projectdevalle.registration.domain.producer;

import br.com.senac.projectdevalle.registration.domain.common.Registrable;
import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.producer.exception.CertificationWithoutValidProofException;
import br.com.senac.projectdevalle.registration.domain.producer.exception.OutsideCoverageAreaException;
import br.com.senac.projectdevalle.registration.domain.producer.exception.ProducerNotEligibleToOperateException;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import br.com.senac.projectdevalle.shared.domain.vo.TaxDocument;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Producer implements Registrable {

    private final UUID id;
    private final UUID userId;
    private String name;
    private final TaxDocument taxDocument;
    private final ProductionType productionType;
    private OriginLocation originLocation;
    private final List<SupportingDocument> supportingDocuments = new ArrayList<>();
    private final List<Certification> certifications = new ArrayList<>();
    private BankDetails bankDetails;
    private DeliveryArea deliveryArea;
    private RegistrationStatus status;
    private String statusReason;

    private Producer(UUID id, UUID userId, String name, TaxDocument taxDocument, ProductionType productionType,
                      OriginLocation originLocation, RegistrationStatus status) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.taxDocument = taxDocument;
        this.productionType = productionType;
        this.originLocation = originLocation;
        this.deliveryArea = DeliveryArea.NONE;
        this.status = status;
    }

    public static Producer register(UUID userId, String name, TaxDocument taxDocument, ProductionType productionType,
                                     OriginLocation originLocation, List<SupportingDocument> supportingDocuments) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (supportingDocuments == null || supportingDocuments.isEmpty()) {
            throw new IllegalArgumentException("at least one supporting document is required");
        }
        Producer producer = new Producer(UUID.randomUUID(), userId, name, taxDocument, productionType,
                originLocation, RegistrationStatus.PENDING);
        producer.supportingDocuments.addAll(supportingDocuments);
        return producer;
    }

    public static Producer reconstitute(UUID id, UUID userId, String name, TaxDocument taxDocument,
                                         ProductionType productionType, OriginLocation originLocation,
                                         List<SupportingDocument> supportingDocuments,
                                         List<Certification> certifications, BankDetails bankDetails,
                                         DeliveryArea deliveryArea, RegistrationStatus status,
                                         String statusReason) {
        Producer producer = new Producer(id, userId, name, taxDocument, productionType, originLocation, status);
        producer.statusReason = statusReason;
        producer.supportingDocuments.addAll(supportingDocuments);
        producer.certifications.addAll(certifications);
        producer.bankDetails = bankDetails;
        producer.deliveryArea = deliveryArea == null ? DeliveryArea.NONE : deliveryArea;
        return producer;
    }

    // RF30.1 — chamado quando a geocodificação assíncrona/tentativa manual resolve as coordenadas
    // de um cadastro que foi salvo com geocodingPending=true.
    public void resolveOriginCoordinates(Coordinates coordinates) {
        this.originLocation = originLocation.withCoordinates(coordinates);
    }

    public boolean isGeocodingPending() {
        return originLocation.coordinates() == null;
    }

    // RN02 — a política de área de cobertura é injetada, nunca hardcoded no agregado.
    public void approve(CoverageAreaPolicy coverageAreaPolicy) {
        if (status != RegistrationStatus.PENDING) {
            throw new BusinessRuleViolationException("Only pending registrations can be approved");
        }
        requireOperableLocation(coverageAreaPolicy, "approve");
        this.status = RegistrationStatus.APPROVED;
        this.statusReason = null;
    }

    // RF40 — reativação de um cadastro suspenso; revalida as mesmas condições da aprovação (RN02/RN30).
    public void reactivate(CoverageAreaPolicy coverageAreaPolicy) {
        if (status != RegistrationStatus.SUSPENDED) {
            throw new BusinessRuleViolationException("Only suspended registrations can be reactivated");
        }
        requireOperableLocation(coverageAreaPolicy, "reactivate");
        this.status = RegistrationStatus.APPROVED;
        this.statusReason = null;
    }

    private void requireOperableLocation(CoverageAreaPolicy coverageAreaPolicy, String action) {
        if (isGeocodingPending()) {
            throw new ProducerNotEligibleToOperateException(
                    "Cannot " + action + " producer without resolved origin coordinates");
        }
        if (!coverageAreaPolicy.covers(originLocation.address())) {
            throw new OutsideCoverageAreaException();
        }
    }

    @Override
    public void reject(String reason) {
        if (status != RegistrationStatus.PENDING) {
            throw new BusinessRuleViolationException("Only pending registrations can be rejected");
        }
        this.statusReason = requireReason(reason);
        this.status = RegistrationStatus.REJECTED;
    }

    @Override
    public void suspend(String reason) {
        if (status != RegistrationStatus.APPROVED) {
            throw new BusinessRuleViolationException("Only approved registrations can be suspended");
        }
        this.statusReason = requireReason(reason);
        this.status = RegistrationStatus.SUSPENDED;
    }

    // RF40 — remoção definitiva do cadastro pela administração.
    @Override
    public void remove(String reason) {
        if (status == RegistrationStatus.REMOVED) {
            throw new BusinessRuleViolationException("Registration is already removed");
        }
        this.statusReason = requireReason(reason);
        this.status = RegistrationStatus.REMOVED;
    }

    private static String requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        return reason.trim();
    }

    // RN01 — só produtores aprovados podem publicar ofertas/operar na plataforma.
    @Override
    public boolean isEligibleToOperate() {
        return status == RegistrationStatus.APPROVED;
    }

    @Override
    public RegistrationStatus status() {
        return status;
    }

    @Override
    public String statusReason() {
        return statusReason;
    }

    // RF04/RN03 — só é anexada se tiver comprovante e validade futura; certificações expiradas
    // deixam de ser válidas dinamicamente via Certification.isValid(Clock), sem precisar remover o registro.
    public void attachCertification(Certification certification, Clock clock) {
        if (!certification.isValid(clock)) {
            throw new CertificationWithoutValidProofException();
        }
        certifications.add(certification);
    }

    // RF05 — corrige o endereço de origem; a geocodificação anterior é descartada
    // (o chamador deve fornecer newOriginLocation já com as coordenadas recalculadas ou pendentes).
    // RN02 — um produtor aprovado não pode mover a origem para fora da área de cobertura; para pendentes e
    // suspensos a cobertura é revalidada na aprovação/reativação.
    public void updateOriginAddress(OriginLocation newOriginLocation, CoverageAreaPolicy coverageAreaPolicy) {
        if (status == RegistrationStatus.APPROVED && !coverageAreaPolicy.covers(newOriginLocation.address())) {
            throw new OutsideCoverageAreaException();
        }
        this.originLocation = newOriginLocation;
    }

    // RF05
    public void updateBankDetails(BankDetails newBankDetails) {
        this.bankDetails = newBankDetails;
    }

    // RF05
    public void updateDeliveryArea(DeliveryArea newDeliveryArea) {
        this.deliveryArea = newDeliveryArea == null ? DeliveryArea.NONE : newDeliveryArea;
    }

    public void updateName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = newName;
    }

    public List<Certification> visibleCertifications(Clock clock) {
        return certifications.stream().filter(certification -> certification.isValid(clock)).toList();
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String name() {
        return name;
    }

    public TaxDocument taxDocument() {
        return taxDocument;
    }

    public ProductionType productionType() {
        return productionType;
    }

    public OriginLocation originLocation() {
        return originLocation;
    }

    public List<SupportingDocument> supportingDocuments() {
        return List.copyOf(supportingDocuments);
    }

    public List<Certification> certifications() {
        return List.copyOf(certifications);
    }

    public BankDetails bankDetails() {
        return bankDetails;
    }

    public DeliveryArea deliveryArea() {
        return deliveryArea;
    }
}
