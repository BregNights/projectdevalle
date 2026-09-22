package br.com.senac.projectdevalle.ordering.infrastructure.persistence;

import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface RecurringOrderJpaRepository extends JpaRepository<RecurringOrderJpaEntity, UUID> {

    List<RecurringOrderJpaEntity> findByRestaurantId(UUID restaurantId);

    List<RecurringOrderJpaEntity> findByStatus(RecurringOrderStatus status);
}
