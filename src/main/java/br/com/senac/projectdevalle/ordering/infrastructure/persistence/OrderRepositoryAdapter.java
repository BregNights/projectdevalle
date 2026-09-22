package br.com.senac.projectdevalle.ordering.infrastructure.persistence;

import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderRepository;
import br.com.senac.projectdevalle.ordering.domain.order.OrderStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// As coleções do pedido são LAZY: o mapeamento para o domínio acontece aqui, dentro da transação.
@Component
class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderEntityMapper mapper;

    OrderRepositoryAdapter(OrderJpaRepository jpaRepository, OrderEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(order)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Order> findByIdForUpdate(UUID id) {
        return jpaRepository.findWithLockById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByRestaurantId(UUID restaurantId) {
        return jpaRepository.findByRestaurantId(restaurantId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByProducerId(UUID producerId) {
        return jpaRepository.findByProducerId(producerId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findInTransitPickedUpBefore(Instant threshold) {
        return jpaRepository.findByStatusAndPickedUpAtBefore(OrderStatus.IN_TRANSIT, threshold).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
