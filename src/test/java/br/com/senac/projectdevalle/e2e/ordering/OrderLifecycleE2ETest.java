package br.com.senac.projectdevalle.e2e.ordering;

import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.RecurrenceType;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.CatalogEntryResponse;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.OfferResponse;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.PublishOfferRequest;
import br.com.senac.projectdevalle.ordering.domain.order.OrderParty;
import br.com.senac.projectdevalle.ordering.domain.order.OrderStatus;
import br.com.senac.projectdevalle.ordering.infrastructure.persistence.OrderStatisticsService;
import br.com.senac.projectdevalle.ordering.infrastructure.web.RecurringOrderController;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.CancelOrderRequest;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.CheckoutRequest;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.CounterProposalRequest;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.DispatchRequest;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.OrderResponse;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.RecurringOrderRequest;
import br.com.senac.projectdevalle.ordering.infrastructure.web.dto.RecurringOrderResponse;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ContactRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.DeliveryAddressResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.LoginRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterProducerRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterRestaurantRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RestaurantRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SupportingDocumentRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TaxDocumentType;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TokenResponse;
import br.com.senac.projectdevalle.support.AbstractIntegrationTest;
import br.com.senac.projectdevalle.support.StubGeocodingPortConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Módulo 1.4 — pedido de ponta a ponta: carrinho, negociação, baixa de estoque, preparo, despacho com
// rastreabilidade, recebimento, cancelamento com multa, taxa de cumprimento e pedido recorrente.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(StubGeocodingPortConfig.class)
class OrderLifecycleE2ETest extends AbstractIntegrationTest {

    private static final String PASSWORD = "S3nhaForte!";

    @LocalServerPort
    private int port;

    @Autowired
    private JwtEncoder jwtEncoder;

    private RestTestClient client;
    private UUID producerId;
    private String producerToken;
    private String restaurantToken;
    private UUID deliveryAddressId;
    private UUID offerId;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        String adminToken = mintToken("ADMINISTRATOR");

        String producerEmail = "produtor+" + UUID.randomUUID() + "@example.com";
        producerId = client.post().uri("/api/v1/producers")
                .body(new RegisterProducerRequest(producerEmail, PASSWORD, "Sítio Boa Terra", TaxDocumentType.CPF,
                        "12345678909", ProductionType.FARMING,
                        new AddressRequest("Rua das Flores", "100", "Centro", "Blumenau", "SC", "89010-000", null),
                        List.of(new SupportingDocumentRequest(SupportingDocumentType.CPF, "12345678909",
                                "https://files/doc.pdf"))))
                .exchange().expectStatus().isCreated()
                .expectBody(ProducerRegistrationResponse.class).returnResult().getResponseBody().producerId();
        post("/api/v1/admin/producers/" + producerId + "/approve", adminToken, null).expectStatus().isNoContent();
        producerToken = login(producerEmail);

        String restaurantEmail = "restaurante+" + UUID.randomUUID() + "@example.com";
        UUID restaurantId = client.post().uri("/api/v1/restaurants")
                .body(new RegisterRestaurantRequest(restaurantEmail, PASSWORD, "Bistrô do Vale", "12345678000195",
                        EstablishmentCategory.BISTRO, new ContactRequest("Ana", "Compras", null, null), "Matriz",
                        new AddressRequest("Av. Brasil", "500", "Centro", "Blumenau", "SC", "89010-000", null)))
                .exchange().expectStatus().isCreated()
                .expectBody(RestaurantRegistrationResponse.class).returnResult().getResponseBody().restaurantId();
        post("/api/v1/admin/restaurants/" + restaurantId + "/approve", adminToken, null).expectStatus().isNoContent();
        restaurantToken = login(restaurantEmail);
        deliveryAddressId = client.get().uri("/api/v1/restaurants/me/delivery-addresses")
                .header("Authorization", "Bearer " + restaurantToken)
                .exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<DeliveryAddressResponse>>() {
                })
                .returnResult().getResponseBody().get(0).id();

        offerId = post("/api/v1/offers", producerToken, new PublishOfferRequest("Alface crespa " + UUID.randomUUID(),
                ProductCategory.VEGETABLES, MeasurementUnit.UNIT, BigDecimal.valueOf(5), BigDecimal.TEN,
                RecurrenceType.ONE_TIME, null, null, LocalDate.now().plusDays(10), List.of()))
                .expectStatus().isCreated()
                .expectBody(OfferResponse.class).returnResult().getResponseBody().id();
    }

    @Test
    void negotiatedOrderIsConfirmedDispatchedWithTraceabilityAndDelivered() {
        // RF16/RF18 — restaurante propõe preço menor.
        OrderResponse order = checkout(4, new BigDecimal("4.50"));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.awaitingResponseFrom()).isEqualTo(OrderParty.PRODUCER);
        assertThat(order.total()).isEqualByComparingTo("18.00");
        UUID itemId = order.items().get(0).id();

        // Restaurante não pode aceitar a própria proposta.
        post("/api/v1/orders/" + order.id() + "/accept", restaurantToken, null).expectStatus().isEqualTo(422);

        // Produtor contrapropõe; restaurante aceita → confirmado e estoque baixado (RN09/RN04).
        OrderResponse countered = post("/api/v1/orders/" + order.id() + "/counter-proposal", producerToken,
                new CounterProposalRequest(List.of(new CounterProposalRequest.ItemTerms(itemId, BigDecimal.valueOf(4),
                        new BigDecimal("4.80"))), null, "Consigo por 4,80"))
                .expectStatus().isOk().expectBody(OrderResponse.class).returnResult().getResponseBody();
        assertThat(countered.awaitingResponseFrom()).isEqualTo(OrderParty.RESTAURANT);
        assertThat(countered.negotiation()).hasSize(2);

        OrderResponse confirmed = action(order.id(), "accept", restaurantToken);
        assertThat(confirmed.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(confirmed.total()).isEqualByComparingTo("19.20");
        assertThat(availableStock()).isEqualByComparingTo("6");

        // Quem não participa do pedido não o enxerga.
        client.get().uri("/api/v1/orders/" + order.id())
                .header("Authorization", "Bearer " + mintToken("PRODUCER"))
                .exchange().expectStatus().isNotFound();

        action(order.id(), "start-preparation", producerToken);
        OrderResponse inTransit = post("/api/v1/orders/" + order.id() + "/dispatch", producerToken,
                new DispatchRequest(List.of(new DispatchRequest.ItemTraceability(itemId, LocalDate.now(), "LOTE-7"))))
                .expectStatus().isOk().expectBody(OrderResponse.class).returnResult().getResponseBody();
        assertThat(inTransit.status()).isEqualTo(OrderStatus.IN_TRANSIT);
        assertThat(inTransit.items().get(0).lot()).isEqualTo("LOTE-7");

        OrderResponse delivered = action(order.id(), "confirm-receipt", restaurantToken);
        assertThat(delivered.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(delivered.history()).hasSize(6);

        // RN11 — taxa de cumprimento do produtor.
        OrderStatisticsService.ProducerFulfillment fulfillment = client.get()
                .uri("/api/v1/producers/" + producerId + "/fulfillment")
                .header("Authorization", "Bearer " + restaurantToken)
                .exchange().expectStatus().isOk()
                .expectBody(OrderStatisticsService.ProducerFulfillment.class).returnResult().getResponseBody();
        assertThat(fulfillment.deliveredOrders()).isEqualTo(1);
        assertThat(fulfillment.fulfillmentRate()).isEqualByComparingTo("100.0");

        // RF42 — indicadores de pedidos no painel.
        OrderStatisticsService.OrderMetrics metrics = client.get().uri("/api/v1/admin/metrics/orders")
                .header("Authorization", "Bearer " + mintToken("ADMINISTRATOR"))
                .exchange().expectStatus().isOk()
                .expectBody(OrderStatisticsService.OrderMetrics.class).returnResult().getResponseBody();
        assertThat(metrics.ordersByStatus().get("DELIVERED")).isPositive();
    }

    // RN10 — cancelamento pelo restaurante depois do início do preparo gera multa; o estoque volta (RN04).
    @Test
    void restaurantCancellingDuringPreparationPaysPenaltyAndStockReturns() {
        OrderResponse order = checkout(2, null);
        action(order.id(), "accept", producerToken);
        assertThat(availableStock()).isEqualByComparingTo("8");
        OrderResponse preparing = action(order.id(), "start-preparation", producerToken);

        OrderResponse restaurantView = client.get().uri("/api/v1/orders/" + order.id())
                .header("Authorization", "Bearer " + restaurantToken)
                .exchange().expectStatus().isOk().expectBody(OrderResponse.class).returnResult().getResponseBody();
        assertThat(preparing.status()).isEqualTo(OrderStatus.IN_PREPARATION);
        assertThat(restaurantView.cancellationPenaltyIfCancelledNow()).isEqualByComparingTo("2.00");

        OrderResponse cancelled = post("/api/v1/orders/" + order.id() + "/cancel", restaurantToken,
                new CancelOrderRequest("Mudança no cardápio"))
                .expectStatus().isOk().expectBody(OrderResponse.class).returnResult().getResponseBody();

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.cancellation().penaltyAmount()).isEqualByComparingTo("2.00");
        assertThat(availableStock()).isEqualByComparingTo("10");
    }

    @Test
    void checkoutRejectsQuantityAboveStockAndRecurringOrderCanBeScheduledAndSuspended() {
        post("/api/v1/orders/checkout", restaurantToken, new CheckoutRequest(deliveryAddressId,
                LocalDate.now().plusDays(2), null,
                List.of(new CheckoutRequest.Item(offerId, BigDecimal.valueOf(11), null))))
                .expectStatus().isEqualTo(422);

        // RF21 — pedido recorrente semanal.
        RecurringOrderResponse recurring = post("/api/v1/recurring-orders", restaurantToken,
                new RecurringOrderRequest(deliveryAddressId, DayOfWeek.FRIDAY, null,
                        List.of(new RecurringOrderRequest.Item(offerId, BigDecimal.valueOf(3)))))
                .expectStatus().isCreated().expectBody(RecurringOrderResponse.class).returnResult().getResponseBody();
        assertThat(recurring.nextDeliveryDate().getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(recurring.items()).singleElement().extracting(RecurringOrderResponse.Item::productName)
                .asString().startsWith("Alface crespa");

        RecurringOrderController.SuspendResponse suspended = post("/api/v1/recurring-orders/" + recurring.id()
                + "/suspend", restaurantToken, null)
                .expectStatus().isOk().expectBody(RecurringOrderController.SuspendResponse.class).returnResult()
                .getResponseBody();
        assertThat(suspended.immediate() || suspended.recurringOrder().suspendAfterNextRun()).isTrue();

        // RF17 — comparação de ofertas equivalentes inclui a própria oferta.
        List<CatalogEntryResponse> equivalents = client.get()
                .uri("/api/v1/catalog/offers/" + offerId + "/equivalents")
                .header("Authorization", "Bearer " + restaurantToken)
                .exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<CatalogEntryResponse>>() {
                })
                .returnResult().getResponseBody();
        assertThat(equivalents).extracting(entry -> entry.offer().id()).contains(offerId);
    }

    private OrderResponse checkout(int quantity, BigDecimal proposedPrice) {
        List<OrderResponse> orders = post("/api/v1/orders/checkout", restaurantToken,
                new CheckoutRequest(deliveryAddressId, LocalDate.now().plusDays(2), "Entregar cedo",
                        List.of(new CheckoutRequest.Item(offerId, BigDecimal.valueOf(quantity), proposedPrice))))
                .expectStatus().isCreated()
                .expectBody(new ParameterizedTypeReference<List<OrderResponse>>() {
                })
                .returnResult().getResponseBody();
        assertThat(orders).hasSize(1);
        return orders.get(0);
    }

    private OrderResponse action(UUID orderId, String action, String token) {
        return post("/api/v1/orders/" + orderId + "/" + action, token, null)
                .expectStatus().isOk().expectBody(OrderResponse.class).returnResult().getResponseBody();
    }

    private BigDecimal availableStock() {
        return client.get().uri("/api/v1/offers/mine")
                .header("Authorization", "Bearer " + producerToken)
                .exchange().expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<OfferResponse>>() {
                })
                .returnResult().getResponseBody().stream()
                .filter(offer -> offer.id().equals(offerId))
                .findFirst().orElseThrow().quantityAvailable();
    }

    private RestTestClient.ResponseSpec post(String uri, String token, Object body) {
        RestTestClient.RequestBodySpec request = client.post().uri(uri).header("Authorization", "Bearer " + token);
        return body == null ? request.exchange() : request.body(body).exchange();
    }

    private String login(String email) {
        return client.post().uri("/api/v1/auth/login")
                .body(new LoginRequest(email, PASSWORD))
                .exchange().expectStatus().isOk()
                .expectBody(TokenResponse.class).returnResult().getResponseBody().accessToken();
    }

    private String mintToken(String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("projectdevalle")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(UUID.randomUUID().toString())
                .claim("role", role)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
