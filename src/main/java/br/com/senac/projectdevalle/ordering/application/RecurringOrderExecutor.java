package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.application.port.OrderingPartiesPort;
import br.com.senac.projectdevalle.ordering.domain.order.DeliveryDestination;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrder;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

// RF21 — gera os pedidos dos recorrentes que chegaram ao dia de execução. Cada recorrente roda na própria
// transação: um problema em um não impede os demais. Itens indisponíveis são pulados e ficam no resumo.
@Component
public class RecurringOrderExecutor {

    private static final Logger log = LoggerFactory.getLogger(RecurringOrderExecutor.class);

    private final RecurringOrderRepository repository;
    private final OrderingPartiesPort partiesPort;
    private final OrderPlacement orderPlacement;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public RecurringOrderExecutor(RecurringOrderRepository repository, OrderingPartiesPort partiesPort,
                                  OrderPlacement orderPlacement, TransactionTemplate transactionTemplate,
                                  Clock clock) {
        this.repository = repository;
        this.partiesPort = partiesPort;
        this.orderPlacement = orderPlacement;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.orders.jobs-interval:PT15M}", initialDelayString = "PT1M")
    public void runDueOrders() {
        for (RecurringOrder candidate : repository.findActive()) {
            if (!candidate.isDue(clock)) {
                continue;
            }
            try {
                transactionTemplate.executeWithoutResult(status -> execute(candidate.id()));
            } catch (RuntimeException exception) {
                log.error("Falha ao executar pedido recorrente {}", candidate.id(), exception);
            }
        }
    }

    // Visível para testes e para execução manual.
    public void execute(UUID recurringOrderId) {
        RecurringOrder recurringOrder = repository.findById(recurringOrderId).orElse(null);
        if (recurringOrder == null || !recurringOrder.isDue(clock)) {
            return;
        }
        String summary;
        if (!partiesPort.isRestaurantEligible(recurringOrder.restaurantId())) {
            summary = "Não executado: cadastro do restaurante não está aprovado.";
        } else {
            DeliveryDestination destination = partiesPort.deliveryAddress(recurringOrder.restaurantId(),
                    recurringOrder.deliveryAddressId()).orElse(null);
            if (destination == null) {
                summary = "Não executado: o endereço de entrega foi removido do cadastro.";
            } else {
                List<OrderPlacement.RequestedItem> items = recurringOrder.items().stream()
                        .map(item -> new OrderPlacement.RequestedItem(item.offerId(), item.quantity(), null))
                        .toList();
                OrderPlacement.Result result = orderPlacement.place(recurringOrder.restaurantId(), destination,
                        recurringOrder.nextDeliveryDate(), recurringOrder.notes(), items, recurringOrder.id(), false);
                summary = result.orders().size() + " pedido(s) gerado(s) para " + recurringOrder.nextDeliveryDate()
                        + (result.skippedItems().isEmpty() ? "." : ". Itens não incluídos: "
                        + String.join("; ", result.skippedItems()) + ".");
            }
        }
        recurringOrder.markExecuted(summary, clock);
        repository.save(recurringOrder);
        log.info("Pedido recorrente {}: {}", recurringOrder.id(), summary);
    }
}
