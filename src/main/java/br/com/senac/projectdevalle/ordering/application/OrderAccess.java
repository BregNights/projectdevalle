package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.application.port.OrderingPartiesPort;
import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderParty;
import br.com.senac.projectdevalle.ordering.domain.order.OrderRepository;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Resolve o usuário logado como parte (restaurante/produtor) e garante que ele só enxergue e mexa nos
// próprios pedidos — pedido de terceiros é tratado como inexistente.
@Component
public class OrderAccess {

    private static final String RESTAURANT_ROLE = "RESTAURANT";
    private static final String PRODUCER_ROLE = "PRODUCER";

    private final OrderRepository orderRepository;
    private final OrderingPartiesPort partiesPort;

    public OrderAccess(OrderRepository orderRepository, OrderingPartiesPort partiesPort) {
        this.orderRepository = orderRepository;
        this.partiesPort = partiesPort;
    }

    public Participant participant(UUID userId, String role) {
        if (RESTAURANT_ROLE.equals(role)) {
            return partiesPort.restaurantIdByUserId(userId)
                    .map(id -> new Participant(OrderParty.RESTAURANT, id))
                    .orElseThrow(() -> new ResourceNotFoundException("No restaurant registered for current user"));
        }
        if (PRODUCER_ROLE.equals(role)) {
            return partiesPort.producerIdByUserId(userId)
                    .map(id -> new Participant(OrderParty.PRODUCER, id))
                    .orElseThrow(() -> new ResourceNotFoundException("No producer registered for current user"));
        }
        throw new BusinessRuleViolationException("Only restaurants and producers take part in orders");
    }

    public Order orderFor(UUID orderId, Participant participant) {
        return orderRepository.findById(orderId)
                .filter(participant::takesPartIn)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    public Order orderForUpdate(UUID orderId, Participant participant) {
        return orderRepository.findByIdForUpdate(orderId)
                .filter(participant::takesPartIn)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    // RN01 — só cadastros aprovados podem fechar/aceitar pedidos.
    public void requireEligible(Participant participant) {
        boolean eligible = participant.party() == OrderParty.RESTAURANT
                ? partiesPort.isRestaurantEligible(participant.partyId())
                : partiesPort.isProducerEligible(participant.partyId());
        if (!eligible) {
            throw new BusinessRuleViolationException("Registration is not approved to operate (RN01)");
        }
    }
}
