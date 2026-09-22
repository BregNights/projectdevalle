package br.com.senac.projectdevalle.ordering.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "recurring_order_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecurringOrderItemJpaEntity {

    @EmbeddedId
    private Key id;

    @MapsId("recurringOrderId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_order_id", nullable = false)
    private RecurringOrderJpaEntity recurringOrder;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private int position;

    @Embeddable
    public record Key(@Column(name = "recurring_order_id") UUID recurringOrderId,
                      @Column(name = "offer_id") UUID offerId) implements Serializable {
    }
}
