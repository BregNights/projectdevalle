package br.com.senac.projectdevalle.e2e.registration;

import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AddDeliveryAddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ContactRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.DeliveryAddressResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.LoginRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterProducerRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterRestaurantRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RestaurantAccountResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RestaurantRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SupportingDocumentRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TaxDocumentType;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TokenResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateRestaurantProfileRequest;
import br.com.senac.projectdevalle.support.AbstractIntegrationTest;
import br.com.senac.projectdevalle.support.StubGeocodingPortConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// RF05 — o próprio restaurante edita perfil/contato e gerencia o CRUD completo de endereços
// de entrega (adicionar, listar, definir principal, remover), respeitando a regra de nunca
// ficar sem nenhum endereço.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(StubGeocodingPortConfig.class)
class EditRestaurantAccountE2ETest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void restaurantEditsProfileAndManagesDeliveryAddresses() {
        RestTestClient client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        String email = "restaurante+" + UUID.randomUUID() + "@example.com";
        String password = "S3nhaForte!";
        registerRestaurant(client, email, password);
        String token = login(client, email, password);

        client.put()
                .uri("/api/v1/restaurants/me/profile")
                .header("Authorization", "Bearer " + token)
                .body(new UpdateRestaurantProfileRequest("Restaurante Bom Sabor Atualizado",
                        EstablishmentCategory.FINE_DINING,
                        new ContactRequest("Carla", "Gerente", "47988887777", "carla@bomsabor.com")))
                .exchange()
                .expectStatus().isNoContent();

        EntityExchangeResult<DeliveryAddressResponse> addResult = client.post()
                .uri("/api/v1/restaurants/me/delivery-addresses")
                .header("Authorization", "Bearer " + token)
                .body(new AddDeliveryAddressRequest("Filial Centro",
                        new AddressRequest("Rua XV", "200", "Centro", "Itajai", "SC", "88301-100", null), false))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DeliveryAddressResponse.class)
                .returnResult();
        UUID filialId = addResult.getResponseBody().id();
        assertThat(addResult.getResponseBody().primary()).isFalse();

        EntityExchangeResult<List<DeliveryAddressResponse>> listResult = client.get()
                .uri("/api/v1/restaurants/me/delivery-addresses")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<DeliveryAddressResponse>>() {
                })
                .returnResult();
        assertThat(listResult.getResponseBody()).hasSize(2);
        UUID matrizId = listResult.getResponseBody().stream()
                .filter(DeliveryAddressResponse::primary)
                .findFirst().orElseThrow().id();

        client.put()
                .uri("/api/v1/restaurants/me/delivery-addresses/" + filialId + "/primary")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        client.delete()
                .uri("/api/v1/restaurants/me/delivery-addresses/" + matrizId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        EntityExchangeResult<RestaurantAccountResponse> accountResult = client.get()
                .uri("/api/v1/restaurants/me/account")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RestaurantAccountResponse.class)
                .returnResult();
        RestaurantAccountResponse account = accountResult.getResponseBody();
        assertThat(account.corporateName()).isEqualTo("Restaurante Bom Sabor Atualizado");
        assertThat(account.contact().name()).isEqualTo("Carla");
        assertThat(account.deliveryAddresses()).singleElement()
                .matches(DeliveryAddressResponse::primary)
                .extracting(DeliveryAddressResponse::label).isEqualTo("Filial Centro");
    }

    // RN — um restaurante nunca pode ficar sem nenhum endereço de entrega.
    @Test
    void cannotRemoveTheLastRemainingDeliveryAddress() {
        RestTestClient client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        String email = "restaurante.unico+" + UUID.randomUUID() + "@example.com";
        String password = "S3nhaForte!";
        registerRestaurant(client, email, password);
        String token = login(client, email, password);

        EntityExchangeResult<List<DeliveryAddressResponse>> listResult = client.get()
                .uri("/api/v1/restaurants/me/delivery-addresses")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<DeliveryAddressResponse>>() {
                })
                .returnResult();
        UUID onlyAddressId = listResult.getResponseBody().get(0).id();

        client.delete()
                .uri("/api/v1/restaurants/me/delivery-addresses/" + onlyAddressId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(ProblemDetail.class);
    }

    // A checagem de posse é feita pelo papel do usuário no JWT (@PreAuthorize), não por um {id} no path —
    // um produtor autenticado nunca deve conseguir chamar os endpoints de restaurante.
    @Test
    void producerAuthenticatedUserCannotCallRestaurantEndpoints() {
        RestTestClient client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        String email = "produtor.intruso+" + UUID.randomUUID() + "@example.com";
        String password = "S3nhaForte!";
        registerProducer(client, email, password);
        String producerToken = login(client, email, password);

        client.get()
                .uri("/api/v1/restaurants/me/account")
                .header("Authorization", "Bearer " + producerToken)
                .exchange()
                .expectStatus().isForbidden();

        client.post()
                .uri("/api/v1/restaurants/me/delivery-addresses")
                .header("Authorization", "Bearer " + producerToken)
                .body(new AddDeliveryAddressRequest("Invasao",
                        new AddressRequest("Rua X", "1", "Centro", "Itajai", "SC", "88301-000", null), false))
                .exchange()
                .expectStatus().isForbidden();
    }

    private void registerRestaurant(RestTestClient client, String email, String password) {
        RegisterRestaurantRequest request = new RegisterRestaurantRequest(
                email, password, "Restaurante Bom Sabor", "12345678000195", EstablishmentCategory.BISTRO,
                new ContactRequest("Maria", "Compras", "47999999999", "maria@bomsabor.com"),
                "Matriz",
                new AddressRequest("Av. Brasil", "500", "Centro", "Itajai", "SC", "88301-000", null));

        client.post()
                .uri("/api/v1/restaurants")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RestaurantRegistrationResponse.class);
    }

    private void registerProducer(RestTestClient client, String email, String password) {
        RegisterProducerRequest request = new RegisterProducerRequest(
                email, password, "Joao Pescador", TaxDocumentType.CPF, "12345678909", ProductionType.FISHING,
                new AddressRequest("Rua das Flores", "100", "Centro", "Itajai", "SC", "88301-000", null),
                List.of(new SupportingDocumentRequest(
                        br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType.FISHING_LICENSE,
                        "REG-12345", "https://files/license.pdf")));

        client.post()
                .uri("/api/v1/producers")
                .body(request)
                .exchange()
                .expectStatus().isCreated();
    }

    private String login(RestTestClient client, String email, String password) {
        EntityExchangeResult<TokenResponse> result = client.post()
                .uri("/api/v1/auth/login")
                .body(new LoginRequest(email, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult();
        return result.getResponseBody().accessToken();
    }
}
