package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.application.command.CreateRecurringOrderCommand;
import br.com.senac.projectdevalle.ordering.application.port.OrderingCatalogPort;
import br.com.senac.projectdevalle.ordering.application.port.OrderingPartiesPort;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrder;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrder.RecurringOrderItem;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrderRepository;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.shared.domain.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

// RF21/RN12 — pedidos recorrentes do restaurante.
@Service
public class RecurringOrderService {

    private final OrderAccess orderAccess;
    private final RecurringOrderRepository repository;
    private final OrderingPartiesPort partiesPort;
    private final OrderingCatalogPort catalogPort;
    private final Duration suspensionNotice;
    private final Clock clock;

    public RecurringOrderService(OrderAccess orderAccess, RecurringOrderRepository repository,
                                 OrderingPartiesPort partiesPort, OrderingCatalogPort catalogPort,
                                 @Value("${app.orders.recurring-suspension-notice:PT48H}") Duration suspensionNotice,
                                 Clock clock) {
        this.orderAccess = orderAccess;
        this.repository = repository;
        this.partiesPort = partiesPort;
        this.catalogPort = catalogPort;
        this.suspensionNotice = suspensionNotice;
        this.clock = clock;
    }

    @Transactional
    public RecurringOrder create(CreateRecurringOrderCommand command) {
        Participant restaurant = orderAccess.participant(command.userId(), "RESTAURANT");
        orderAccess.requireEligible(restaurant);
        partiesPort.deliveryAddress(restaurant.partyId(), command.deliveryAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery address not found: " + command.deliveryAddressId()));
        for (RecurringOrderItem item : command.items() == null ? List.<RecurringOrderItem>of() : command.items()) {
            boolean purchasable = catalogPort.findOffer(item.offerId())
                    .map(OrderingCatalogPort.OfferSnapshot::purchasable)
                    .orElse(false);
            if (!purchasable) {
                throw new BusinessRuleViolationException("Offer is not available in the catalog: " + item.offerId());
            }
        }
        return repository.save(RecurringOrder.create(restaurant.partyId(), command.deliveryAddressId(),
                command.deliveryDay(), command.items(), command.notes(), clock));
    }

    @Transactional(readOnly = true)
    public List<RecurringOrder> mine(UUID userId) {
        Participant restaurant = orderAccess.participant(userId, "RESTAURANT");
        return repository.findByRestaurantId(restaurant.partyId()).stream()
                .sorted(Comparator.comparing(RecurringOrder::createdAt).reversed())
                .toList();
    }

    // RN12 — devolve true se suspendeu na hora; false se a próxima execução ainda ocorre (aviso < mínimo).
    @Transactional
    public SuspensionResult suspend(UUID recurringOrderId, UUID userId) {
        RecurringOrder recurringOrder = owned(recurringOrderId, userId);
        boolean immediate = recurringOrder.suspend(suspensionNotice, clock);
        return new SuspensionResult(repository.save(recurringOrder), immediate);
    }

    @Transactional
    public RecurringOrder resume(UUID recurringOrderId, UUID userId) {
        RecurringOrder recurringOrder = owned(recurringOrderId, userId);
        orderAccess.requireEligible(orderAccess.participant(userId, "RESTAURANT"));
        recurringOrder.resume(clock);
        return repository.save(recurringOrder);
    }

    private RecurringOrder owned(UUID recurringOrderId, UUID userId) {
        Participant restaurant = orderAccess.participant(userId, "RESTAURANT");
        return repository.findById(recurringOrderId)
                .filter(order -> order.restaurantId().equals(restaurant.partyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Recurring order not found: " + recurringOrderId));
    }

    public record SuspensionResult(RecurringOrder recurringOrder, boolean immediate) {
    }
}
