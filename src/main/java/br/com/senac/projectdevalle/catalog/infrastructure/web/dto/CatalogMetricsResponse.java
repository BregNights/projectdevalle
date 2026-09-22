package br.com.senac.projectdevalle.catalog.infrastructure.web.dto;

import java.util.Map;

public record CatalogMetricsResponse(
        long totalOffers,
        Map<String, Long> offersByStatus,
        Map<String, Long> activeOffersByCategory,
        long producersWithActiveOffers
) {
}
