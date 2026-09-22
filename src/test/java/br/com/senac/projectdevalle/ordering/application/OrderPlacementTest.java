package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.application.port.OrderingCatalogPort;
import br.com.senac.projectdevalle.ordering.application.port.OrderingCatalogPort.OfferSnapshot;
import br.com.senac.projectdevalle.ordering.domain.order.DeliveryDestination;
import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPlacementTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate DELIVERY = LocalDate.of(2026, 6, 18);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderingCatalogPort catalogPort;

    private OrderPlacement placement;

    @BeforeEach
    void setUp() {
        placement = new OrderPlacement(orderRepository, catalogPort, CLOCK);
        lenient().when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // RF16 — um carrinho com dois produtores gera dois pedidos, com o mesmo checkoutId.
    @Test
    void splitsTheCartIntoOneOrderPerProducer() {
        UUID producerA = UUID.randomUUID();
        UUID producerB = UUID.randomUUID();
        OfferSnapshot alface = offer(producerA, "Alface", BigDecimal.TEN, null);
        OfferSnapshot couve = offer(producerA, "Couve", BigDecimal.TEN, null);
        OfferSnapshot tainha = offer(producerB, "Tainha", BigDecimal.TEN, null);
        List.of(alface, couve, tainha).forEach(o -> when(catalogPort.findOffer(o.offerId())).thenReturn(Optional.of(o)));

        OrderPlacement.Result result = placement.place(UUID.randomUUID(), destination(), DELIVERY, null, List.of(
                request(alface, 2), request(couve, 1), request(tainha, 3)), null, true);

        assertThat(result.orders()).hasSize(2);
        assertThat(result.orders()).extracting(Order::producerId).containsExactly(producerA, producerB);
        assertThat(result.orders().get(0).items()).hasSize(2);
        assertThat(result.orders()).extracting(Order::checkoutId).containsOnly(result.orders().get(0).checkoutId());
    }

    @Test
    void strictModeRejectsQuantityAboveTheAvailableStock() {
        OfferSnapshot alface = offer(UUID.randomUUID(), "Alface", BigDecimal.valueOf(5), null);
        when(catalogPort.findOffer(alface.offerId())).thenReturn(Optional.of(alface));

        assertThatThrownBy(() -> placement.place(UUID.randomUUID(), destination(), DELIVERY, null,
                List.of(request(alface, 6)), null, true))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Alface");
        verify(orderRepository, never()).save(any());
    }

    // RN06 — não se pede entrega depois da validade da oferta.
    @Test
    void strictModeRejectsDeliveryAfterTheOfferDeadline() {
        OfferSnapshot peixe = offer(UUID.randomUUID(), "Peixe", BigDecimal.TEN, LocalDate.of(2026, 6, 17));
        when(catalogPort.findOffer(peixe.offerId())).thenReturn(Optional.of(peixe));

        assertThatThrownBy(() -> placement.place(UUID.randomUUID(), destination(), DELIVERY, null,
                List.of(request(peixe, 1)), null, true))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    // RF21 — no recorrente, itens indisponíveis são pulados e informados.
    @Test
    void tolerantModeSkipsUnavailableItems() {
        OfferSnapshot alface = offer(UUID.randomUUID(), "Alface", BigDecimal.TEN, null);
        UUID missing = UUID.randomUUID();
        when(catalogPort.findOffer(alface.offerId())).thenReturn(Optional.of(alface));
        when(catalogPort.findOffer(missing)).thenReturn(Optional.empty());

        OrderPlacement.Result result = placement.place(UUID.randomUUID(), destination(), DELIVERY, null, List.of(
                request(alface, 1), new OrderPlacement.RequestedItem(missing, BigDecimal.ONE, null)),
                UUID.randomUUID(), false);

        assertThat(result.orders()).hasSize(1);
        assertThat(result.skippedItems()).hasSize(1);
    }

    private static OfferSnapshot offer(UUID producerId, String name, BigDecimal available, LocalDate until) {
        return new OfferSnapshot(UUID.randomUUID(), producerId, name, "VEGETABLES", "UNIT", BigDecimal.valueOf(5),
                available, null, until, true);
    }

    private static OrderPlacement.RequestedItem request(OfferSnapshot offer, int quantity) {
        return new OrderPlacement.RequestedItem(offer.offerId(), BigDecimal.valueOf(quantity), null);
    }

    private static DeliveryDestination destination() {
        return new DeliveryDestination(UUID.randomUUID(), "Matriz",
                new Address("Rua XV", "10", "Centro", "Blumenau", "SC", "89010-000", null), null);
    }
}
