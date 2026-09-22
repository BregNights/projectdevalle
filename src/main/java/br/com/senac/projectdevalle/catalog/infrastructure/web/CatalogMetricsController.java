package br.com.senac.projectdevalle.catalog.infrastructure.web;

import br.com.senac.projectdevalle.catalog.infrastructure.persistence.CatalogMetricsService;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.CatalogMetricsResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// RF42 — indicadores do catálogo, restritos à administração (RNF08).
@RestController
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class CatalogMetricsController {

    private final CatalogMetricsService catalogMetricsService;

    public CatalogMetricsController(CatalogMetricsService catalogMetricsService) {
        this.catalogMetricsService = catalogMetricsService;
    }

    @GetMapping("/api/v1/admin/metrics/catalog")
    public CatalogMetricsResponse metrics() {
        return catalogMetricsService.collect();
    }
}
