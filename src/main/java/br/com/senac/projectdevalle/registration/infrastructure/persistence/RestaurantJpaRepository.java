package br.com.senac.projectdevalle.registration.infrastructure.persistence;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface RestaurantJpaRepository extends JpaRepository<RestaurantJpaEntity, UUID> {

    Optional<RestaurantJpaEntity> findByUserId(UUID userId);

    List<RestaurantJpaEntity> findByStatus(RegistrationStatus status);
}
