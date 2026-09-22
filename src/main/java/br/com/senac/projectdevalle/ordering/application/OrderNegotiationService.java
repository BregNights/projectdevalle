package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.application.port.OrderingCatalogPort;
import br.com.senac.projectdevalle.ordering.domain.order.ItemTerms;
import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderItem;
import br.com.senac.projectdevalle.ordering.domain.order.OrderRepository;
import br.com.senac.projectdevalle.ordering.domain.order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// RF18 — negociação assistida: quem tem a vez aceita ou faz contraproposta de preço/quantidade/data.
@Service
public class OrderNegotiationService {

    private final OrderAccess orderAccess;
    private final OrderRepository orderRepository;
    private final OrderingCatalogPort catalogPort;
    private final Clock clock;

    public OrderNegotiationService(OrderAccess orderAccess, OrderRepository orderRepository,
                                   OrderingCatalogPort catalogPort, Clock clock) {
        this.orderAccess = orderAccess;
        this.orderRepository = orderRepository;
        this.catalogPort = catalogPort;
        this.clock = clock;
    }

    // RN09 — ao aceitar, o pedido é confirmado e o estoque da oferta é baixado na mesma transação
    // (se faltar estoque, nada é confirmado).
    @Transactional
    public Order accept(UUID orderId, UUID userId, String role) {
        Participant participant = orderAccess.participant(userId, role);
        Order order = orderAccess.orderForUpdate(orderId, participant);
        orderAccess.requireEligible(participant);
        order.accept(participant.party(), clock);
        if (order.status() == OrderStatus.CONFIRMED) {
            for (OrderItem item : order.items()) {
                catalogPort.reserveStock(item.offerId(), item.quantity());
            }
        }
        return orderRepository.save(order);
    }

    @Transactional
    public Order counterPropose(UUID orderId, UUID userId, String role, List<ItemTerms> terms,
                                LocalDate newDeliveryDate, String message) {
        Participant participant = orderAccess.participant(userId, role);
        Order order = orderAccess.orderForUpdate(orderId, participant);
        orderAccess.requireEligible(participant);
        order.counterPropose(participant.party(), terms, newDeliveryDate, message, clock);
        return orderRepository.save(order);
    }
}
