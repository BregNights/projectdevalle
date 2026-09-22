package br.com.senac.projectdevalle.ordering.infrastructure.persistence;

import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrderStatus;
import br.com.senac.projectdevalle.shared.infrastructure.persistence.AuditableJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recurring_orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecurringOrderJpaEntity extends AuditableJpaEntity {

    @Id
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "delivery_address_id", nullable = false)
    private UUID deliveryAddressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_day", nullable = false)
    private DayOfWeek deliveryDay;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringOrderStatus status;

    @Column(name = "next_delivery_date", nullable = false)
    private LocalDate nextDeliveryDate;

    @Column(name = "suspend_after_next_run", nullable = false)
    private boolean suspendAfterNextRun;

    @Column(name = "last_run_at")
    private Instant lastRunAt;

    @Column(name = "last_run_summary")
    private String lastRunSummary;

    @Builder.Default
    @BatchSize(size = 50)
    @OrderBy("position")
    @OneToMany(mappedBy = "recurringOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecurringOrderItemJpaEntity> items = new ArrayList<>();
}
