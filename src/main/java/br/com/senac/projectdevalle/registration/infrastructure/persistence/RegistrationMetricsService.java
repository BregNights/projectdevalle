package br.com.senac.projectdevalle.registration.infrastructure.persistence;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import br.com.senac.projectdevalle.registration.domain.user.Role;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AdminMetricsResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

// Serviço de leitura para o painel de administração (RF42). Consulta os repositórios JPA
// diretamente (em vez de passar pelos agregados de domínio) porque isso é relatório, não
// regra de negócio — não há invariante a proteger ao contar linhas para um dashboard.
@Service
public class RegistrationMetricsService {

    private static final int TOP_CITIES_LIMIT = 5;

    private final ProducerJpaRepository producerJpaRepository;
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final Clock clock;

    RegistrationMetricsService(ProducerJpaRepository producerJpaRepository,
                               RestaurantJpaRepository restaurantJpaRepository,
                               UserJpaRepository userJpaRepository, Clock clock) {
        this.producerJpaRepository = producerJpaRepository;
        this.restaurantJpaRepository = restaurantJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.clock = clock;
    }

    public AdminMetricsResponse collect() {
        Instant now = Instant.now(clock);
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);

        Map<String, Long> producersByStatus = new LinkedHashMap<>();
        Map<String, Long> restaurantsByStatus = new LinkedHashMap<>();
        for (RegistrationStatus status : RegistrationStatus.values()) {
            producersByStatus.put(status.name(), producerJpaRepository.countByStatus(status));
            restaurantsByStatus.put(status.name(), restaurantJpaRepository.countByStatus(status));
        }

        Map<String, Long> producersByProductionType = new LinkedHashMap<>();
        for (ProductionType type : ProductionType.values()) {
            producersByProductionType.put(type.name(), producerJpaRepository.countByProductionType(type));
        }

        Map<String, Long> restaurantsByCategory = new LinkedHashMap<>();
        for (EstablishmentCategory category : EstablishmentCategory.values()) {
            restaurantsByCategory.put(category.name(), restaurantJpaRepository.countByCategory(category));
        }

        Map<String, Long> usersByRole = new LinkedHashMap<>();
        for (Role role : Role.values()) {
            usersByRole.put(role.name(), userJpaRepository.countByRole(role));
        }

        Map<String, Long> topProducerCities = new LinkedHashMap<>();
        for (ProducerJpaRepository.CityCount cityCount : producerJpaRepository
                .countGroupedByOriginCity(PageRequest.of(0, TOP_CITIES_LIMIT))) {
            topProducerCities.put(cityCount.getCity(), cityCount.getTotal());
        }

        var recentUsers = userJpaRepository.findTop8ByOrderByCreatedAtDesc().stream()
                .map(user -> new AdminMetricsResponse.RecentUser(user.getEmail(), user.getRole().name(),
                        user.isActive(), user.getCreatedAt()))
                .toList();

        return new AdminMetricsResponse(
                producerJpaRepository.count(),
                restaurantJpaRepository.count(),
                userJpaRepository.count(),
                userJpaRepository.countByActive(true),
                producersByStatus,
                restaurantsByStatus,
                producersByProductionType,
                restaurantsByCategory,
                usersByRole,
                producerJpaRepository.countByGeocodingPendingTrue(),
                producerJpaRepository.countByCreatedAtAfter(sevenDaysAgo),
                restaurantJpaRepository.countByCreatedAtAfter(sevenDaysAgo),
                producerJpaRepository.countByCreatedAtAfter(thirtyDaysAgo),
                restaurantJpaRepository.countByCreatedAtAfter(thirtyDaysAgo),
                topProducerCities,
                recentUsers);
    }
}
