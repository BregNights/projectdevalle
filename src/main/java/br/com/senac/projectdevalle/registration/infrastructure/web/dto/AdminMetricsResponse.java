package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

// RF42 — indicadores do painel de administração. Cobre só o que existe dado real hoje
// (Cadastro): volume transacionado, ticket médio e taxa de cancelamento ficam para quando
// Pedidos/Pagamentos existirem — não fazem sentido calculados sobre dado que não existe.
public record AdminMetricsResponse(
        long totalProducers,
        long totalRestaurants,
        long totalUsers,
        long activeUsers,
        Map<String, Long> producersByStatus,
        Map<String, Long> restaurantsByStatus,
        Map<String, Long> producersByProductionType,
        Map<String, Long> restaurantsByCategory,
        Map<String, Long> usersByRole,
        long producersGeocodingPending,
        long newProducersLast7Days,
        long newRestaurantsLast7Days,
        long newProducersLast30Days,
        long newRestaurantsLast30Days,
        Map<String, Long> topProducerCities,
        List<RecentUser> recentUsers
) {

    public record RecentUser(String email, String role, boolean active, Instant createdAt) {
    }
}
