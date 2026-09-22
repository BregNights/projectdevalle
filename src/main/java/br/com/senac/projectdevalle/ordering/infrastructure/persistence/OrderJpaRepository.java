package br.com.senac.projectdevalle.ordering.infrastructure.persistence;

import br.com.senac.projectdevalle.ordering.domain.order.OrderParty;
import br.com.senac.projectdevalle.ordering.domain.order.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {

    List<OrderJpaEntity> findByRestaurantId(UUID restaurantId);

    List<OrderJpaEntity> findByProducerId(UUID producerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrderJpaEntity> findWithLockById(UUID id);

    List<OrderJpaEntity> findByStatusAndPickedUpAtBefore(OrderStatus status, Instant threshold);

    // RN11 — base da taxa de cumprimento do produtor.
    long countByProducerIdAndStatus(UUID producerId, OrderStatus status);

    long countByProducerIdAndStatusAndCancelledByAndCancelledAfterConfirmationTrue(UUID producerId,
                                                                                  OrderStatus status,
                                                                                  OrderParty cancelledBy);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderJpaEntity o WHERE o.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") OrderStatus status);

    // RF42 — tempo médio de entrega: da confirmação do pedido até o recebimento, nos pedidos entregues.
    @Query("SELECT o.confirmedAt AS confirmedAt, o.deliveredAt AS deliveredAt FROM OrderJpaEntity o "
            + "WHERE o.status = :status AND o.confirmedAt IS NOT NULL AND o.deliveredAt IS NOT NULL")
    List<DeliveryTimes> findDeliveryTimes(@Param("status") OrderStatus status);

    interface DeliveryTimes {

        Instant getConfirmedAt();

        Instant getDeliveredAt();
    }
}
