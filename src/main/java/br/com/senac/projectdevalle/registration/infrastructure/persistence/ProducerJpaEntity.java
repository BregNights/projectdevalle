package br.com.senac.projectdevalle.registration.infrastructure.persistence;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.producer.BankAccountType;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.shared.infrastructure.crypto.EncryptedStringConverter;
import br.com.senac.projectdevalle.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "producers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProducerJpaEntity extends AuditableJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "production_type", nullable = false)
    private ProductionType productionType;

    @Column(name = "origin_street", nullable = false)
    private String originStreet;

    @Column(name = "origin_number", nullable = false)
    private String originNumber;

    @Column(name = "origin_neighborhood", nullable = false)
    private String originNeighborhood;

    @Column(name = "origin_city", nullable = false)
    private String originCity;

    @Column(name = "origin_state", nullable = false)
    private String originState;

    @Column(name = "origin_zip_code", nullable = false)
    private String originZipCode;

    @Column(name = "origin_complement")
    private String originComplement;

    @Column(name = "origin_latitude")
    private Double originLatitude;

    @Column(name = "origin_longitude")
    private Double originLongitude;

    @Column(name = "geocoding_pending", nullable = false)
    private boolean geocodingPending;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bank_name")
    private String bankName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bank_agency")
    private String bankAgency;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bank_account")
    private String bankAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_account_type")
    private BankAccountType bankAccountType;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bank_account_holder")
    private String bankAccountHolder;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "delivery_area")
    private List<String> deliveryArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @Column(name = "status_reason")
    private String statusReason;

    @Builder.Default
    @OneToMany(mappedBy = "producer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProducerSupportingDocumentJpaEntity> supportingDocuments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "producer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProducerCertificationJpaEntity> certifications = new ArrayList<>();
}
