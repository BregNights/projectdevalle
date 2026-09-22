package br.com.senac.projectdevalle.ordering.infrastructure.web;

import br.com.senac.projectdevalle.ordering.application.RecurringOrderService;
import br.com.senac.projectdevalle.ordering.application.command.CreateRecurringOrderCommand;
import br.com.senac.projectdevalle.ordering.application.port.OrderingCatalogPort;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrder;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrder.RecurringOrderItem;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.RecurringOrderRequest;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.RecurringOrderResponse;
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

import java.util.List;
import java.util.UUID;

// RF21/RN12 — pedidos recorrentes do restaurante.
@RestController
@RequestMapping("/api/v1/recurring-orders")
@PreAuthorize("hasRole('RESTAURANT')")
public class RecurringOrderController {

    private final RecurringOrderService recurringOrderService;
    private final OrderingCatalogPort catalogPort;

    public RecurringOrderController(RecurringOrderService recurringOrderService, OrderingCatalogPort catalogPort) {
        this.recurringOrderService = recurringOrderService;
        this.catalogPort = catalogPort;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RecurringOrderResponse create(@AuthenticationPrincipal Jwt jwt,
                                         @Valid @RequestBody RecurringOrderRequest request) {
        RecurringOrder created = recurringOrderService.create(new CreateRecurringOrderCommand(userId(jwt),
                request.deliveryAddressId(), request.deliveryDay(), request.notes(), request.items().stream()
                .map(item -> new RecurringOrderItem(item.offerId(), item.quantity()))
                .toList()));
        return toResponse(created);
    }

    @GetMapping
    public List<RecurringOrderResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return recurringOrderService.mine(userId(jwt)).stream().map(this::toResponse).toList();
    }

    @PostMapping("/{id}/suspend")
    public SuspendResponse suspend(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        RecurringOrderService.SuspensionResult result = recurringOrderService.suspend(id, userId(jwt));
        return new SuspendResponse(toResponse(result.recurringOrder()), result.immediate());
    }

    @PostMapping("/{id}/resume")
    public RecurringOrderResponse resume(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return toResponse(recurringOrderService.resume(id, userId(jwt)));
    }

    // immediate=false: aviso menor que o mínimo (RN12) — a próxima execução ainda acontece e depois suspende.
    public record SuspendResponse(RecurringOrderResponse recurringOrder, boolean immediate) {
    }

    private RecurringOrderResponse toResponse(RecurringOrder recurringOrder) {
        return RecurringOrderResponse.from(recurringOrder, offerId -> catalogPort.findOffer(offerId)
                .map(OrderingCatalogPort.OfferSnapshot::productName)
                .orElse("Oferta indisponível"));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
