package br.com.senac.projectdevalle.ordering.infrastructure.persistence;

import br.com.senac.projectdevalle.ordering.domain.order.OrderParty;
import br.com.senac.projectdevalle.ordering.domain.order.OrderStatus;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderJpaEntity extends AuditableJpaEntity {

    @Id
    private UUID id;

    @Column(name = "checkout_id", nullable = false)
    private UUID checkoutId;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "producer_id", nullable = false)
    private UUID producerId;

    @Column(name = "delivery_address_id")
    private UUID deliveryAddressId;

    @Column(name = "delivery_label")
    private String deliveryLabel;

    @Column(name = "delivery_street", nullable = false)
    private String deliveryStreet;

    @Column(name = "delivery_number", nullable = false)
    private String deliveryNumber;

    @Column(name = "delivery_neighborhood", nullable = false)
    private String deliveryNeighborhood;

    @Column(name = "delivery_city", nullable = false)
    private String deliveryCity;

    @Column(name = "delivery_state", nullable = false)
    private String deliveryState;

    @Column(name = "delivery_zip_code", nullable = false)
    private String deliveryZipCode;

    @Column(name = "delivery_complement")
    private String deliveryComplement;

    @Column(name = "delivery_latitude")
    private Double deliveryLatitude;

    @Column(name = "delivery_longitude")
    private Double deliveryLongitude;

    @Column(name = "requested_delivery_date", nullable = false)
    private LocalDate requestedDeliveryDate;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "awaiting_response_from")
    private OrderParty awaitingResponseFrom;

    // Redundante (soma dos itens), mas gravado para os indicadores do painel (RF42) sem recalcular.
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "preparation_started_at")
    private Instant preparationStartedAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "delivery_auto_confirmed", nullable = false)
    private boolean deliveryAutoConfirmed;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by")
    private OrderParty cancelledBy;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancelled_after_confirmation")
    private Boolean cancelledAfterConfirmation;

    @Column(name = "penalty_amount")
    private BigDecimal penaltyAmount;

    @Column(name = "recurring_order_id")
    private UUID recurringOrderId;

    @Builder.Default
    @BatchSize(size = 50)
    @OrderBy("position")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 50)
    @OrderBy("roundNumber")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderNegotiationRoundJpaEntity> negotiationRounds = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 50)
    @OrderBy("position")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusChangeJpaEntity> statusChanges = new ArrayList<>();
}
