package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.application.port.OrderingCatalogPort;
import br.com.senac.projectdevalle.ordering.application.port.OrderingCatalogPort.OfferSnapshot;
import br.com.senac.projectdevalle.ordering.domain.order.DeliveryDestination;
import br.com.senac.projectdevalle.ordering.domain.order.NewOrderItem;
import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderRepository;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Monta e grava os pedidos de um carrinho: valida cada item contra a oferta atual e separa um pedido por
// produtor (RF16). Usado pelo checkout (modo estrito: qualquer item inválido barra tudo) e pelos pedidos
// recorrentes (modo tolerante: itens indisponíveis são pulados e informados — RF21).
@Component
public class OrderPlacement {

    private final OrderRepository orderRepository;
    private final OrderingCatalogPort catalogPort;
    private final Clock clock;

    public OrderPlacement(OrderRepository orderRepository, OrderingCatalogPort catalogPort, Clock clock) {
        this.orderRepository = orderRepository;
        this.catalogPort = catalogPort;
        this.clock = clock;
    }

    public Result place(UUID restaurantId, DeliveryDestination destination, LocalDate deliveryDate, String notes,
                        List<RequestedItem> requested, UUID recurringOrderId, boolean strict) {
        if (requested == null || requested.isEmpty()) {
            throw new IllegalArgumentException("the cart is empty");
        }
        Map<UUID, List<NewOrderItem>> itemsByProducer = new LinkedHashMap<>();
        List<String> skipped = new ArrayList<>();
        for (RequestedItem item : requested) {
            String problem = null;
            OfferSnapshot offer = catalogPort.findOffer(item.offerId()).orElse(null);
            if (offer == null || !offer.purchasable()) {
                problem = "oferta indisponível no catálogo";
            } else if (item.quantity() == null || item.quantity().signum() <= 0) {
                problem = "quantidade inválida";
            } else if (offer.quantityAvailable().compareTo(item.quantity()) < 0) {
                problem = "quantidade maior que a disponível (" + offer.quantityAvailable().stripTrailingZeros()
                        .toPlainString() + ")";
            } else if (offer.availableFrom() != null && deliveryDate != null
                    && deliveryDate.isBefore(offer.availableFrom())) {
                problem = "entrega antes do início da disponibilidade (" + offer.availableFrom() + ")";
            } else if (offer.availableUntil() != null && deliveryDate != null
                    && deliveryDate.isAfter(offer.availableUntil())) {
                // RN06 — não se entrega produto depois da validade da oferta.
                problem = "entrega depois da validade da oferta (" + offer.availableUntil() + ")";
            }
            String label = offer != null ? offer.productName() : item.offerId().toString();
            if (problem != null) {
                if (strict) {
                    throw new BusinessRuleViolationException(label + ": " + problem);
                }
                skipped.add(label + ": " + problem);
                continue;
            }
            itemsByProducer.computeIfAbsent(offer.producerId(), key -> new ArrayList<>())
                    .add(new NewOrderItem(offer.offerId(), offer.productName(), offer.category(), offer.unit(),
                            offer.price(), item.quantity(), item.proposedUnitPrice()));
        }
        UUID checkoutId = UUID.randomUUID();
        List<Order> orders = new ArrayList<>();
        itemsByProducer.forEach((producerId, items) -> orders.add(orderRepository.save(Order.place(checkoutId,
                restaurantId, producerId, destination, deliveryDate, notes, items, recurringOrderId, clock))));
        return new Result(orders, skipped);
    }

    public record RequestedItem(UUID offerId, BigDecimal quantity, BigDecimal proposedUnitPrice) {
    }

    public record Result(List<Order> orders, List<String> skippedItems) {
    }
}
