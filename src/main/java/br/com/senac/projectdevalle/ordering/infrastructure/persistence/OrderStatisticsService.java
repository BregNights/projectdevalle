package br.com.senac.projectdevalle.ordering.infrastructure.persistence;

import br.com.senac.projectdevalle.ordering.domain.order.OrderParty;
import br.com.senac.projectdevalle.ordering.domain.order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Relatórios de pedidos (contagens e somas), consultando o repositório JPA diretamente como os demais serviços
// de métricas — não há regra de negócio a proteger aqui.
@Service
public class OrderStatisticsService {

    private final OrderJpaRepository orderJpaRepository;

    OrderStatisticsService(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    // RN11 — taxa de cumprimento = entregues ÷ (entregues + cancelados pelo produtor depois de confirmados).
    // Recusar um pedido ainda pendente não conta contra o produtor.
    @Transactional(readOnly = true)
    public ProducerFulfillment producerFulfillment(UUID producerId) {
        long delivered = orderJpaRepository.countByProducerIdAndStatus(producerId, OrderStatus.DELIVERED);
        long cancelledByProducer = orderJpaRepository
                .countByProducerIdAndStatusAndCancelledByAndCancelledAfterConfirmationTrue(producerId,
                        OrderStatus.CANCELLED, OrderParty.PRODUCER);
        long base = delivered + cancelledByProducer;
        BigDecimal rate = base == 0 ? null : BigDecimal.valueOf(delivered * 100)
                .divide(BigDecimal.valueOf(base), 1, RoundingMode.HALF_UP);
        return new ProducerFulfillment(producerId, delivered, cancelledByProducer, rate);
    }

    // RF42 — volume transacionado, ticket médio, taxa de cancelamento e tempo médio de entrega.
    @Transactional(readOnly = true)
    public OrderMetrics metrics() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.put(status.name(), orderJpaRepository.countByStatus(status));
        }
        long delivered = byStatus.get(OrderStatus.DELIVERED.name());
        long cancelled = byStatus.get(OrderStatus.CANCELLED.name());
        BigDecimal volume = orderJpaRepository.sumTotalByStatus(OrderStatus.DELIVERED);
        BigDecimal averageTicket = delivered == 0 ? null
                : volume.divide(BigDecimal.valueOf(delivered), 2, RoundingMode.HALF_UP);
        BigDecimal cancellationRate = delivered + cancelled == 0 ? null
                : BigDecimal.valueOf(cancelled * 100).divide(BigDecimal.valueOf(delivered + cancelled), 1,
                RoundingMode.HALF_UP);
        List<OrderJpaRepository.DeliveryTimes> times = orderJpaRepository.findDeliveryTimes(OrderStatus.DELIVERED);
        BigDecimal averageDeliveryHours = times.isEmpty() ? null : BigDecimal.valueOf(times.stream()
                        .mapToLong(time -> Duration.between(time.getConfirmedAt(), time.getDeliveredAt()).toMinutes())
                        .average()
                        .orElse(0) / 60.0)
                .setScale(1, RoundingMode.HALF_UP);
        return new OrderMetrics(orderJpaRepository.count(), byStatus, volume, averageTicket, cancellationRate,
                averageDeliveryHours);
    }

    public record ProducerFulfillment(UUID producerId, long deliveredOrders, long producerCancellations,
                                      BigDecimal fulfillmentRate) {
    }

    // cancellationRate: cancelados ÷ pedidos finalizados (entregues + cancelados), em %.
    // averageDeliveryHours: da confirmação ao recebimento, nos pedidos entregues.
    public record OrderMetrics(long totalOrders, Map<String, Long> ordersByStatus, BigDecimal deliveredVolume,
                               BigDecimal averageTicket, BigDecimal cancellationRate,
                               BigDecimal averageDeliveryHours) {
    }
}
