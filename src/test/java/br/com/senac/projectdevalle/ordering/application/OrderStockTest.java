package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.application.port.OrderingCatalogPort;
import br.com.senac.projectdevalle.ordering.application.port.OrderingSettingsPort;
import br.com.senac.projectdevalle.ordering.domain.order.DeliveryDestination;
import br.com.senac.projectdevalle.ordering.domain.order.NewOrderItem;
import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderParty;
import br.com.senac.projectdevalle.ordering.domain.order.OrderRepository;
import br.com.senac.projectdevalle.ordering.domain.order.OrderStatus;
import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// RN09/RN04 — o aceite baixa o estoque; o cancelamento de pedido confirmado devolve.
@ExtendWith(MockitoExtension.class)
class OrderStockTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private OrderAccess orderAccess;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderingCatalogPort catalogPort;

    @Mock
    private OrderingSettingsPort settingsPort;

    private OrderNegotiationService negotiationService;
    private OrderFulfillmentService fulfillmentService;
    private final UUID userId = UUID.randomUUID();
    private final UUID offerId = UUID.randomUUID();
    private Order order;
    private final Participant producer = new Participant(OrderParty.PRODUCER, UUID.randomUUID());

    @BeforeEach
    void setUp() {
        negotiationService = new OrderNegotiationService(orderAccess, orderRepository, catalogPort, CLOCK);
        fulfillmentService = new OrderFulfillmentService(orderAccess, orderRepository, catalogPort, settingsPort,
                CLOCK);
        order = Order.place(UUID.randomUUID(), UUID.randomUUID(), producer.partyId(),
                new DeliveryDestination(UUID.randomUUID(), "Matriz",
                        new Address("Rua XV", "10", "Centro", "Blumenau", "SC", "89010-000", null), null),
                LocalDate.of(2026, 6, 18), null, List.of(new NewOrderItem(offerId, "Alface", "VEGETABLES", "UNIT",
                        BigDecimal.valueOf(5), BigDecimal.valueOf(4), null)), null, CLOCK);
        when(orderAccess.participant(userId, "PRODUCER")).thenReturn(producer);
        when(orderAccess.orderForUpdate(order.id(), producer)).thenReturn(order);
        lenient().when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void acceptingReservesStockForEachItem() {
        Order accepted = negotiationService.accept(order.id(), userId, "PRODUCER");

        assertThat(accepted.status()).isEqualTo(OrderStatus.CONFIRMED);
        verify(catalogPort).reserveStock(offerId, BigDecimal.valueOf(4));
    }

    @Test
    void acceptanceFailsWhenStockIsInsufficient() {
        doThrow(new BusinessRuleViolationException("Insufficient stock"))
                .when(catalogPort).reserveStock(offerId, BigDecimal.valueOf(4));

        assertThatThrownBy(() -> negotiationService.accept(order.id(), userId, "PRODUCER"))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancellingAConfirmedOrderReleasesTheStock() {
        order.accept(OrderParty.PRODUCER, CLOCK);
        when(settingsPort.cancellationPenaltyPercentage()).thenReturn(BigDecimal.valueOf(20));

        fulfillmentService.cancel(order.id(), userId, "PRODUCER", "Quebra de safra");

        verify(catalogPort).releaseStock(offerId, BigDecimal.valueOf(4));
    }

    @Test
    void rejectingAPendingOrderDoesNotTouchTheStock() {
        when(settingsPort.cancellationPenaltyPercentage()).thenReturn(BigDecimal.valueOf(20));

        fulfillmentService.cancel(order.id(), userId, "PRODUCER", "Sem produção");

        verify(catalogPort, never()).releaseStock(any(), any());
    }
}
