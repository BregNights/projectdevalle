package br.com.senac.projectdevalle.registration.infrastructure.persistence;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RestaurantJpaEntity extends AuditableJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "corporate_name", nullable = false)
    private String corporateName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstablishmentCategory category;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "contact_role")
    private String contactRole;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status;

    @Column(name = "status_reason")
    private String statusReason;

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RestaurantDeliveryAddressJpaEntity> deliveryAddresses = new ArrayList<>();
}
