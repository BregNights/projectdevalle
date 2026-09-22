package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.application.command.CheckoutCommand;
import br.com.senac.projectdevalle.ordering.application.port.OrderingPartiesPort;
import br.com.senac.projectdevalle.ordering.domain.order.DeliveryDestination;
import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// RF16 — fechamento do carrinho: gera um pedido por produtor, todos no endereço de entrega escolhido (RF30.2).
@Service
public class CheckoutService {

    private final OrderAccess orderAccess;
    private final OrderingPartiesPort partiesPort;
    private final OrderPlacement orderPlacement;

    public CheckoutService(OrderAccess orderAccess, OrderingPartiesPort partiesPort, OrderPlacement orderPlacement) {
        this.orderAccess = orderAccess;
        this.partiesPort = partiesPort;
        this.orderPlacement = orderPlacement;
    }

    @Transactional
    public List<Order> checkout(CheckoutCommand command) {
        Participant restaurant = orderAccess.participant(command.userId(), "RESTAURANT");
        orderAccess.requireEligible(restaurant);
        DeliveryDestination destination = partiesPort.deliveryAddress(restaurant.partyId(),
                        command.deliveryAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery address not found: " + command.deliveryAddressId()));
        List<OrderPlacement.RequestedItem> items = command.items() == null ? List.of() : command.items().stream()
                .map(item -> new OrderPlacement.RequestedItem(item.offerId(), item.quantity(),
                        item.proposedUnitPrice()))
                .toList();
        return orderPlacement.place(restaurant.partyId(), destination, command.requestedDeliveryDate(),
                command.notes(), items, null, true).orders();
    }
}
