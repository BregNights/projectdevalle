package br.com.senac.projectdevalle.ordering.infrastructure.persistence;

import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrder;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrder.RecurringOrderItem;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrderRepository;
import br.com.senac.projectdevalle.ordering.domain.recurring.RecurringOrderStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class RecurringOrderRepositoryAdapter implements RecurringOrderRepository {

    private final RecurringOrderJpaRepository jpaRepository;

    RecurringOrderRepositoryAdapter(RecurringOrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public RecurringOrder save(RecurringOrder recurringOrder) {
        RecurringOrderJpaEntity entity = RecurringOrderJpaEntity.builder()
                .id(recurringOrder.id())
                .restaurantId(recurringOrder.restaurantId())
                .deliveryAddressId(recurringOrder.deliveryAddressId())
                .deliveryDay(recurringOrder.deliveryDay())
                .notes(recurringOrder.notes())
                .status(recurringOrder.status())
                .nextDeliveryDate(recurringOrder.nextDeliveryDate())
                .suspendAfterNextRun(recurringOrder.suspendAfterNextRun())
                .lastRunAt(recurringOrder.lastRunAt())
                .lastRunSummary(recurringOrder.lastRunSummary())
                .build();
        List<RecurringOrderItem> items = recurringOrder.items();
        for (int position = 0; position < items.size(); position++) {
            RecurringOrderItem item = items.get(position);
            entity.getItems().add(RecurringOrderItemJpaEntity.builder()
                    .id(new RecurringOrderItemJpaEntity.Key(recurringOrder.id(), item.offerId()))
                    .recurringOrder(entity)
                    .quantity(item.quantity())
                    .position(position)
                    .build());
        }
        return toDomain(jpaRepository.save(entity), recurringOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RecurringOrder> findById(UUID id) {
        return jpaRepository.findById(id).map(entity -> toDomain(entity, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringOrder> findByRestaurantId(UUID restaurantId) {
        return jpaRepository.findByRestaurantId(restaurantId).stream().map(entity -> toDomain(entity, null)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringOrder> findActive() {
        return jpaRepository.findByStatus(RecurringOrderStatus.ACTIVE).stream()
                .map(entity -> toDomain(entity, null))
                .toList();
    }

    // createdAt vem da auditoria JPA; logo após o primeiro save ela pode ainda não estar populada no retorno,
    // então se aproveita a do domínio recém-criado.
    private static RecurringOrder toDomain(RecurringOrderJpaEntity entity, RecurringOrder source) {
        List<RecurringOrderItem> items = entity.getItems().stream()
                .map(item -> new RecurringOrderItem(item.getId().offerId(), item.getQuantity()))
                .toList();
        return RecurringOrder.reconstitute(entity.getId(), entity.getRestaurantId(), entity.getDeliveryAddressId(),
                entity.getDeliveryDay(), items, entity.getNotes(), entity.getStatus(), entity.getNextDeliveryDate(),
                entity.isSuspendAfterNextRun(), entity.getLastRunAt(), entity.getLastRunSummary(),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : source != null ? source.createdAt() : null);
    }
}
