package br.com.senac.projectdevalle.ordering.domain.recurring;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringOrderRepository {

    RecurringOrder save(RecurringOrder recurringOrder);

    Optional<RecurringOrder> findById(UUID id);

    List<RecurringOrder> findByRestaurantId(UUID restaurantId);

    List<RecurringOrder> findActive();
}
