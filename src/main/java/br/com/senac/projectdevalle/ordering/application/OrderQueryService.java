package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderParty;
import br.com.senac.projectdevalle.ordering.domain.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class OrderQueryService {

    private final OrderAccess orderAccess;
    private final OrderRepository orderRepository;

    public OrderQueryService(OrderAccess orderAccess, OrderRepository orderRepository) {
        this.orderAccess = orderAccess;
        this.orderRepository = orderRepository;
    }

    // Pedidos do restaurante ou do produtor logado, mais recentes primeiro.
    @Transactional(readOnly = true)
    public List<Order> mine(UUID userId, String role) {
        Participant participant = orderAccess.participant(userId, role);
        List<Order> orders = participant.party() == OrderParty.RESTAURANT
                ? orderRepository.findByRestaurantId(participant.partyId())
                : orderRepository.findByProducerId(participant.partyId());
        return orders.stream().sorted(Comparator.comparing(Order::placedAt).reversed()).toList();
    }

    @Transactional(readOnly = true)
    public Order one(UUID orderId, UUID userId, String role) {
        return orderAccess.orderFor(orderId, orderAccess.participant(userId, role));
    }
}
