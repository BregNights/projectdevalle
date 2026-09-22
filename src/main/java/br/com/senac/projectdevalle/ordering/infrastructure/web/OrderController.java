package br.com.senac.projectdevalle.ordering.infrastructure.web;

import br.com.senac.projectdevalle.ordering.application.CheckoutService;
import br.com.senac.projectdevalle.ordering.application.OrderFulfillmentService;
import br.com.senac.projectdevalle.ordering.application.OrderNegotiationService;
import br.com.senac.projectdevalle.ordering.application.OrderQueryService;
import br.com.senac.projectdevalle.ordering.application.command.CheckoutCommand;
import br.com.senac.projectdevalle.ordering.application.port.OrderingPartiesPort;
import br.com.senac.projectdevalle.ordering.application.port.OrderingSettingsPort;
import br.com.senac.projectdevalle.ordering.domain.order.ItemTerms;
import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderParty;
import br.com.senac.projectdevalle.ordering.domain.order.Traceability;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.CancelOrderRequest;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.CheckoutRequest;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.CounterProposalRequest;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.DispatchRequest;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// Módulo 1.4 — pedidos (RF16–RF20). Cada pedido é visível e operável apenas pelo restaurante e pelo produtor
// envolvidos; a vez de cada parte e o que ela pode fazer são validados no domínio.
@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasAnyRole('RESTAURANT', 'PRODUCER')")
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderNegotiationService negotiationService;
    private final OrderFulfillmentService fulfillmentService;
    private final OrderQueryService queryService;
    private final OrderingPartiesPort partiesPort;
    private final OrderingSettingsPort settingsPort;

    public OrderController(CheckoutService checkoutService, OrderNegotiationService negotiationService,
                           OrderFulfillmentService fulfillmentService, OrderQueryService queryService,
                           OrderingPartiesPort partiesPort, OrderingSettingsPort settingsPort) {
        this.checkoutService = checkoutService;
        this.negotiationService = negotiationService;
        this.fulfillmentService = fulfillmentService;
        this.queryService = queryService;
        this.partiesPort = partiesPort;
        this.settingsPort = settingsPort;
    }

    // RF16 — fecha o carrinho; devolve um pedido por produtor.
    @PreAuthorize("hasRole('RESTAURANT')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/checkout")
    public List<OrderResponse> checkout(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CheckoutRequest request) {
        List<Order> orders = checkoutService.checkout(new CheckoutCommand(userId(jwt), request.deliveryAddressId(),
                request.requestedDeliveryDate(), request.notes(), request.items().stream()
                .map(item -> new CheckoutCommand.CheckoutItem(item.offerId(), item.quantity(),
                        item.proposedUnitPrice()))
                .toList()));
        return orders.stream().map(order -> toResponse(order, OrderParty.RESTAURANT)).toList();
    }

    @GetMapping
    public List<OrderResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        OrderParty viewer = viewer(jwt);
        return queryService.mine(userId(jwt), role(jwt)).stream().map(order -> toResponse(order, viewer)).toList();
    }

    @GetMapping("/{id}")
    public OrderResponse one(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return toResponse(queryService.one(id, userId(jwt), role(jwt)), viewer(jwt));
    }

    // RF18 — aceitar os termos vigentes (confirma o pedido e baixa o estoque — RN09).
    @PostMapping("/{id}/accept")
    public OrderResponse accept(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return toResponse(negotiationService.accept(id, userId(jwt), role(jwt)), viewer(jwt));
    }

    @PostMapping("/{id}/counter-proposal")
    public OrderResponse counterPropose(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                        @Valid @RequestBody CounterProposalRequest request) {
        List<ItemTerms> terms = request.items().stream()
                .map(item -> new ItemTerms(item.itemId(), item.quantity(), item.unitPrice()))
                .toList();
        return toResponse(negotiationService.counterPropose(id, userId(jwt), role(jwt), terms, request.deliveryDate(),
                request.message()), viewer(jwt));
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @PostMapping("/{id}/start-preparation")
    public OrderResponse startPreparation(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return toResponse(fulfillmentService.startPreparation(id, userId(jwt), role(jwt)), viewer(jwt));
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @PostMapping("/{id}/dispatch")
    public OrderResponse dispatch(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                  @Valid @RequestBody DispatchRequest request) {
        List<Traceability> traceability = request.items().stream()
                .map(item -> new Traceability(item.itemId(), item.harvestDate(), item.lot()))
                .toList();
        return toResponse(fulfillmentService.dispatch(id, userId(jwt), role(jwt), traceability), viewer(jwt));
    }

    @PreAuthorize("hasRole('RESTAURANT')")
    @PostMapping("/{id}/confirm-receipt")
    public OrderResponse confirmReceipt(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return toResponse(fulfillmentService.confirmReceipt(id, userId(jwt), role(jwt)), viewer(jwt));
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                @Valid @RequestBody CancelOrderRequest request) {
        return toResponse(fulfillmentService.cancel(id, userId(jwt), role(jwt), request.reason()), viewer(jwt));
    }

    private OrderResponse toResponse(Order order, OrderParty viewer) {
        BigDecimal penaltyPercentage = settingsPort.cancellationPenaltyPercentage();
        return OrderResponse.from(order, viewer, partiesPort.restaurantName(order.restaurantId()),
                partiesPort.producerName(order.producerId()), penaltyPercentage);
    }

    private static OrderParty viewer(Jwt jwt) {
        return "PRODUCER".equals(role(jwt)) ? OrderParty.PRODUCER : OrderParty.RESTAURANT;
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static String role(Jwt jwt) {
        return jwt.getClaimAsString("role");
    }
}
