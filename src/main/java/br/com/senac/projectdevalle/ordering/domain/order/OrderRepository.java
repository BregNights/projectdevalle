package br.com.senac.projectdevalle.ordering.domain.order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    // Com lock no banco: usado por toda operação que altera o pedido, para duas ações simultâneas (ex.: aceite do
    // produtor e cancelamento do restaurante) não se sobreporem.
    Optional<Order> findByIdForUpdate(UUID id);

    List<Order> findByRestaurantId(UUID restaurantId);

    List<Order> findByProducerId(UUID producerId);

    // RN14 — pedidos em transporte há mais tempo que o prazo de confirmação automática.
    List<Order> findInTransitPickedUpBefore(Instant threshold);
}
