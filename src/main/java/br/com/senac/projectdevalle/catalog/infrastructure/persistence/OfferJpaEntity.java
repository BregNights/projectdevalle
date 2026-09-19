package br.com.senac.projectdevalle.catalog.infrastructure.persistence;

import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.OfferStatus;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.RecurrenceType;
import br.com.senac.projectdevalle.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OfferJpaEntity extends AuditableJpaEntity {

    @Id
    private UUID id;

    @Column(name = "producer_id", nullable = false)
    private UUID producerId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeasurementUnit unit;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "quantity_available", nullable = false)
    private BigDecimal quantityAvailable;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false)
    private RecurrenceType recurrenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_day_of_week")
    private DayOfWeek recurrenceDayOfWeek;

    @Column(name = "availability_from")
    private LocalDate availabilityFrom;

    @Column(name = "availability_until")
    private LocalDate availabilityUntil;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "photo_urls")
    private List<String> photoUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;
}
