package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

// RN14 — o restaurante que não confirma o recebimento (nem abre disputa) dentro do prazo tem o recebimento
// confirmado automaticamente.
@Component
public class DeliveryAutoConfirmationJob {

    private static final Logger log = LoggerFactory.getLogger(DeliveryAutoConfirmationJob.class);

    private final OrderRepository orderRepository;
    private final Duration deadline;
    private final Clock clock;

    public DeliveryAutoConfirmationJob(OrderRepository orderRepository,
                                       @Value("${app.orders.delivery-auto-confirm-after:PT72H}") Duration deadline,
                                       Clock clock) {
        this.orderRepository = orderRepository;
        this.deadline = deadline;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.orders.jobs-interval:PT15M}", initialDelayString = "PT1M")
    @Transactional
    public void run() {
        int confirmed = 0;
        for (Order order : orderRepository.findInTransitPickedUpBefore(Instant.now(clock).minus(deadline))) {
            if (order.autoConfirmReceipt(deadline, clock)) {
                orderRepository.save(order);
                confirmed++;
            }
        }
        if (confirmed > 0) {
            log.info("RN14: {} pedido(s) com recebimento confirmado automaticamente", confirmed);
        }
    }
}
