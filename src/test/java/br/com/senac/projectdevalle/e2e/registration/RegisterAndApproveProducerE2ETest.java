package br.com.senac.projectdevalle.e2e.registration;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.LoginRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerResponse;
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
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(StubGeocodingPortConfig.class)
class RegisterAndApproveProducerE2ETest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void registersLogsInAndGetsApprovedByAdmin() {
        RestTestClient client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        RegisterProducerRequest registerRequest = new RegisterProducerRequest(
                "joao.pescador+" + UUID.randomUUID() + "@example.com",
                "S3nhaForte!",
                "Joao Pescador",
                TaxDocumentType.CPF,
                "12345678909",
                ProductionType.FISHING,
                new AddressRequest("Rua das Flores", "100", "Centro", "Itajaí", "SC", "88301-000", null),
                List.of(new SupportingDocumentRequest(SupportingDocumentType.FISHING_LICENSE, "REG-12345",
                        "https://files/license.pdf")));

        EntityExchangeResult<ProducerRegistrationResponse> registerResult = client.post()
                .uri("/api/v1/producers")
                .body(registerRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProducerRegistrationResponse.class)
                .returnResult();
        UUID producerId = registerResult.getResponseBody().producerId();
        assertThat(registerResult.getResponseBody().geocodingPending()).isFalse();

        EntityExchangeResult<TokenResponse> loginResult = client.post()
                .uri("/api/v1/auth/login")
                .body(new LoginRequest(registerRequest.email(), registerRequest.password()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult();
        assertThat(loginResult.getResponseBody().accessToken()).isNotBlank();

        String adminToken = mintToken("ADMINISTRATOR");

        EntityExchangeResult<ProducerResponse> beforeApproval = client.get()
                .uri("/api/v1/producers/" + producerId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProducerResponse.class)
                .returnResult();
        assertThat(beforeApproval.getResponseBody().status()).isEqualTo(RegistrationStatus.PENDING);

        client.post()
                .uri("/api/v1/admin/producers/" + producerId + "/approve")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNoContent();

        EntityExchangeResult<ProducerResponse> afterApproval = client.get()
                .uri("/api/v1/producers/" + producerId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProducerResponse.class)
                .returnResult();
        assertThat(afterApproval.getResponseBody().status()).isEqualTo(RegistrationStatus.APPROVED);
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
