package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.application.port.OrderingCatalogPort;
import br.com.senac.projectdevalle.ordering.application.port.OrderingSettingsPort;
import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderItem;
import br.com.senac.projectdevalle.ordering.domain.order.OrderRepository;
import br.com.senac.projectdevalle.ordering.domain.order.Traceability;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

// RF19/RF20 — andamento do pedido (preparo, despacho, recebimento) e cancelamento.
@Service
public class OrderFulfillmentService {

    private final OrderAccess orderAccess;
    private final OrderRepository orderRepository;
    private final OrderingCatalogPort catalogPort;
    private final OrderingSettingsPort settingsPort;
    private final Clock clock;

    public OrderFulfillmentService(OrderAccess orderAccess, OrderRepository orderRepository,
                                   OrderingCatalogPort catalogPort, OrderingSettingsPort settingsPort, Clock clock) {
        this.orderAccess = orderAccess;
        this.orderRepository = orderRepository;
        this.catalogPort = catalogPort;
        this.settingsPort = settingsPort;
        this.clock = clock;
    }

    @Transactional
    public Order startPreparation(UUID orderId, UUID userId, String role) {
        Participant participant = orderAccess.participant(userId, role);
        Order order = orderAccess.orderForUpdate(orderId, participant);
        order.startPreparation(participant.party(), clock);
        return orderRepository.save(order);
    }

    @Transactional
    public Order dispatch(UUID orderId, UUID userId, String role, List<Traceability> traceability) {
        Participant participant = orderAccess.participant(userId, role);
        Order order = orderAccess.orderForUpdate(orderId, participant);
        order.dispatch(participant.party(), traceability, clock);
        return orderRepository.save(order);
    }

    @Transactional
    public Order confirmReceipt(UUID orderId, UUID userId, String role) {
        Participant participant = orderAccess.participant(userId, role);
        Order order = orderAccess.orderForUpdate(orderId, participant);
        order.confirmReceipt(participant.party(), clock);
        return orderRepository.save(order);
    }

    // RF20/RN10/RN11 — se o pedido já tinha baixado estoque, a quantidade volta para a oferta.
    @Transactional
    public Order cancel(UUID orderId, UUID userId, String role, String reason) {
        Participant participant = orderAccess.participant(userId, role);
        Order order = orderAccess.orderForUpdate(orderId, participant);
        boolean heldStock = order.cancel(participant.party(), reason, settingsPort.cancellationPenaltyPercentage(),
                clock);
        if (heldStock) {
            for (OrderItem item : order.items()) {
                catalogPort.releaseStock(item.offerId(), item.quantity());
            }
        }
        return orderRepository.save(order);
    }
}
