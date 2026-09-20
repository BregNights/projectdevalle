package br.com.senac.projectdevalle.e2e.registration;

import br.com.senac.projectdevalle.registration.domain.producer.BankAccountType;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ContactRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.LoginRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerAccountResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterProducerRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterRestaurantRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RestaurantRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SupportingDocumentRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TaxDocumentType;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TokenResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateBankDetailsRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateDeliveryAreaRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateOriginAddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateOriginAddressResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateProducerProfileRequest;
import br.com.senac.projectdevalle.support.AbstractIntegrationTest;
import br.com.senac.projectdevalle.support.StubGeocodingPortConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// RF05 — o próprio produtor edita perfil, endereço de origem, dados bancários e área de entrega,
// e nenhum outro papel consegue mexer nesses endpoints (a falha de posse corrigida nesta sessão).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(StubGeocodingPortConfig.class)
class EditProducerAccountE2ETest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void producerEditsOwnProfileBankDetailsDeliveryAreaAndOriginAddress() {
        RestTestClient client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        String email = "joao.produtor+" + UUID.randomUUID() + "@example.com";
        String password = "S3nhaForte!";
        registerProducer(client, email, password);
        String token = login(client, email, password);

        client.put()
                .uri("/api/v1/producers/me/profile")
                .header("Authorization", "Bearer " + token)
                .body(new UpdateProducerProfileRequest("Joao Pescador Atualizado"))
                .exchange()
                .expectStatus().isNoContent();

        client.put()
                .uri("/api/v1/producers/me/bank-details")
                .header("Authorization", "Bearer " + token)
                .body(new UpdateBankDetailsRequest("Banco do Brasil", "1234", "56789-0",
                        BankAccountType.CHECKING, "Joao Pescador"))
                .exchange()
                .expectStatus().isNoContent();

        client.put()
                .uri("/api/v1/producers/me/delivery-area")
                .header("Authorization", "Bearer " + token)
                .body(new UpdateDeliveryAreaRequest(List.of("Blumenau", "Itajai")))
                .exchange()
                .expectStatus().isNoContent();

        EntityExchangeResult<UpdateOriginAddressResponse> originResult = client.put()
                .uri("/api/v1/producers/me/origin-address")
                .header("Authorization", "Bearer " + token)
                .body(new UpdateOriginAddressRequest(new AddressRequest("Rua Nova", "50", "Bairro Novo",
                        "Itajai", "SC", "88300-000", null)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UpdateOriginAddressResponse.class)
                .returnResult();
        assertThat(originResult.getResponseBody().geocodingPending()).isFalse();

        EntityExchangeResult<ProducerAccountResponse> accountResult = client.get()
                .uri("/api/v1/producers/me/account")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProducerAccountResponse.class)
                .returnResult();
        ProducerAccountResponse account = accountResult.getResponseBody();
        assertThat(account.name()).isEqualTo("Joao Pescador Atualizado");
        assertThat(account.bankDetails().bankName()).isEqualTo("Banco do Brasil");
        assertThat(account.deliveryAreaMunicipalities()).containsExactly("Blumenau", "Itajai");
        assertThat(account.originAddress().street()).isEqualTo("Rua Nova");
        assertThat(account.geocodingPending()).isFalse();
    }

    // A checagem de posse é feita pelo papel do usuário no JWT (@PreAuthorize), não por um {id} no path —
    // um restaurante autenticado nunca deve conseguir chamar os endpoints de produtor.
    @Test
    void restaurantAuthenticatedUserCannotCallProducerEndpoints() {
        RestTestClient client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        String email = "restaurante.intruso+" + UUID.randomUUID() + "@example.com";
        String password = "S3nhaForte!";
        registerRestaurant(client, email, password);
        String restaurantToken = login(client, email, password);

        client.put()
                .uri("/api/v1/producers/me/bank-details")
                .header("Authorization", "Bearer " + restaurantToken)
                .body(new UpdateBankDetailsRequest("Banco Qualquer", "0000", "00000-0",
                        BankAccountType.CHECKING, "Invasor"))
                .exchange()
                .expectStatus().isForbidden();

        client.get()
                .uri("/api/v1/producers/me/account")
                .header("Authorization", "Bearer " + restaurantToken)
                .exchange()
                .expectStatus().isForbidden();
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
                .expectStatus().isCreated()
                .expectBody(ProducerRegistrationResponse.class);
    }

    private void registerRestaurant(RestTestClient client, String email, String password) {
        RegisterRestaurantRequest request = new RegisterRestaurantRequest(
                email, password, "Restaurante Intruso", "12345678000195", EstablishmentCategory.BISTRO,
                new ContactRequest("Maria", "Compras", "47999999999", "maria@intruso.com"),
                "Matriz",
                new AddressRequest("Rua XV", "500", "Centro", "Itajai", "SC", "88301-000", null));

        client.post()
                .uri("/api/v1/restaurants")
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RestaurantRegistrationResponse.class);
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
