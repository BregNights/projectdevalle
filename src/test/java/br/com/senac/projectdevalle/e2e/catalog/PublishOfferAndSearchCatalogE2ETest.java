package br.com.senac.projectdevalle.e2e.catalog;

import br.com.senac.projectdevalle.catalog.domain.offer.MeasurementUnit;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.catalog.domain.offer.RecurrenceType;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.CatalogEntryResponse;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.OfferResponse;
import br.com.senac.projectdevalle.catalog.infrastructure.web.dto.PublishOfferRequest;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.LoginRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterProducerRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SupportingDocumentRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TaxDocumentType;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TokenResponse;
import br.com.senac.projectdevalle.support.AbstractIntegrationTest;
import br.com.senac.projectdevalle.support.StubGeocodingPortConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(StubGeocodingPortConfig.class)
class PublishOfferAndSearchCatalogE2ETest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void publishedOfferByAnApprovedProducerShowsUpInCatalogSearch() {
        RestTestClient client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        String email = "maria.produtora+" + UUID.randomUUID() + "@example.com";
        String password = "S3nhaForte!";

        RegisterProducerRequest registerRequest = new RegisterProducerRequest(
                email, password, "Maria Produtora", TaxDocumentType.CPF, "12345678909", ProductionType.FARMING,
                new AddressRequest("Rua das Flores", "100", "Centro", "Blumenau", "SC", "89010-000", null),
                List.of(new SupportingDocumentRequest(SupportingDocumentType.CPF, "12345678909",
                        "https://files/doc.pdf")));

        UUID producerId = client.post()
                .uri("/api/v1/producers")
                .body(registerRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProducerRegistrationResponse.class)
                .returnResult()
                .getResponseBody()
                .producerId();

        String adminToken = mintToken("ADMINISTRATOR");
        client.post()
                .uri("/api/v1/admin/producers/" + producerId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNoContent();

        String producerToken = client.post()
                .uri("/api/v1/auth/login")
                .body(new LoginRequest(email, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult()
                .getResponseBody()
                .accessToken();

        String productName = "Alface crespa " + UUID.randomUUID();
        PublishOfferRequest publishRequest = new PublishOfferRequest(productName, ProductCategory.VEGETABLES,
                MeasurementUnit.UNIT, BigDecimal.valueOf(4.5), BigDecimal.valueOf(50), RecurrenceType.ONE_TIME,
                null, null, java.time.LocalDate.of(2027, 12, 31), List.of());

        EntityExchangeResult<OfferResponse> publishResult = client.post()
                .uri("/api/v1/offers")
                .header("Authorization", "Bearer " + producerToken)
                .body(publishRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OfferResponse.class)
                .returnResult();
        assertThat(publishResult.getResponseBody().status().name()).isEqualTo("ACTIVE");

        EntityExchangeResult<List<CatalogEntryResponse>> catalogResult = client.get()
                .uri("/api/v1/catalog?category=VEGETABLES")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<CatalogEntryResponse>>() {
                })
                .returnResult();

        assertThat(catalogResult.getResponseBody())
                .extracting(entry -> entry.offer().productName())
                .contains(productName);

        CatalogEntryResponse entry = catalogResult.getResponseBody().stream()
                .filter(candidate -> candidate.offer().productName().equals(productName))
                .findFirst()
                .orElseThrow();
        assertThat(entry.producerId()).isEqualTo(producerId);
        assertThat(entry.producerName()).isEqualTo("Maria Produtora");
        assertThat(entry.producerCity()).isEqualTo("Blumenau");
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
