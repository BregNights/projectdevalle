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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(StubGeocodingPortConfig.class)
class RegisterAndApproveProducerE2ETest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void registersLogsInAndGetsApprovedByAdmin() {
        RegisterProducerRequest registerRequest = new RegisterProducerRequest(
                "joao.pescador+" + UUID.randomUUID() + "@example.com",
                "S3nhaForte!",
                "Joao Pescador",
                TaxDocumentType.CPF,
                "12345678909",
                ProductionType.FISHING,
                new AddressRequest("Rua das Flores", "100", "Centro", "Itajai", "SC", "88301-000", null),
                List.of(new SupportingDocumentRequest(SupportingDocumentType.FISHING_LICENSE, "REG-12345",
                        "https://files/license.pdf")));

        ResponseEntity<ProducerRegistrationResponse> registerResponse = restTemplate.postForEntity(
                "/api/v1/producers", registerRequest, ProducerRegistrationResponse.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID producerId = registerResponse.getBody().producerId();
        assertThat(registerResponse.getBody().geocodingPending()).isFalse();

        ResponseEntity<TokenResponse> loginResponse = restTemplate.postForEntity("/api/v1/auth/login",
                new LoginRequest(registerRequest.email(), registerRequest.password()), TokenResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody().accessToken()).isNotBlank();

        String adminToken = mintToken("ADMINISTRATOR");
        HttpEntity<Void> adminAuthEntity = new HttpEntity<>(authHeaders(adminToken));

        ResponseEntity<ProducerResponse> beforeApproval = restTemplate.exchange(
                "/api/v1/producers/" + producerId, HttpMethod.GET, adminAuthEntity,
                ProducerResponse.class);
        assertThat(beforeApproval.getBody().status()).isEqualTo(RegistrationStatus.PENDING);

        ResponseEntity<Void> approveResponse = restTemplate.exchange(
                "/api/v1/admin/producers/" + producerId + "/approve", HttpMethod.POST,
                adminAuthEntity, Void.class);
        assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ProducerResponse> afterApproval = restTemplate.exchange(
                "/api/v1/producers/" + producerId, HttpMethod.GET, adminAuthEntity,
                ProducerResponse.class);
        assertThat(afterApproval.getBody().status()).isEqualTo(RegistrationStatus.APPROVED);
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

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
