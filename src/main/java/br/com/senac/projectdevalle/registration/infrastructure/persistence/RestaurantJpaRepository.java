package br.com.senac.projectdevalle.registration.infrastructure.persistence;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface RestaurantJpaRepository extends JpaRepository<RestaurantJpaEntity, UUID> {

    Optional<RestaurantJpaEntity> findByUserId(UUID userId);

    List<RestaurantJpaEntity> findByStatus(RegistrationStatus status);

    long countByStatus(RegistrationStatus status);

    long countByCategory(EstablishmentCategory category);

    long countByCreatedAtAfter(Instant instant);
}
