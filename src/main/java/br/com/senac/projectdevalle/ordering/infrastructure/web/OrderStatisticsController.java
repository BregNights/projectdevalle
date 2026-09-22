package br.com.senac.projectdevalle.ordering.infrastructure.web;

import br.com.senac.projectdevalle.ordering.infrastructure.persistence.OrderStatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class OrderStatisticsController {

    private final OrderStatisticsService statisticsService;

    public OrderStatisticsController(OrderStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    // RN11/RF17 — taxa de cumprimento do produtor, exibida ao comparar ofertas e no perfil.
    @GetMapping("/api/v1/producers/{producerId}/fulfillment")
    public OrderStatisticsService.ProducerFulfillment producerFulfillment(@PathVariable UUID producerId) {
        return statisticsService.producerFulfillment(producerId);
    }

    // RF42 — indicadores de pedidos do painel da administração.
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/api/v1/admin/metrics/orders")
    public OrderStatisticsService.OrderMetrics metrics() {
        return statisticsService.metrics();
    }
}
