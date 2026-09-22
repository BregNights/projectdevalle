package br.com.senac.projectdevalle.e2e.registration;

import br.com.senac.projectdevalle.registration.domain.producer.BankAccountType;
import br.com.senac.projectdevalle.registration.domain.producer.ProductionType;
import br.com.senac.projectdevalle.registration.domain.producer.SupportingDocumentType;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.AddressRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.LoginRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerAccountResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.ProducerRegistrationResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.RegisterProducerRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.SupportingDocumentRequest;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TaxDocumentType;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.TokenResponse;
import br.com.senac.projectdevalle.registration.infrastructure.web.dto.UpdateBankDetailsRequest;
import br.com.senac.projectdevalle.storage.infrastructure.web.dto.StoredFileResponse;
import br.com.senac.projectdevalle.support.AbstractIntegrationTest;
import br.com.senac.projectdevalle.support.StubGeocodingPortConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// RF04/RF07 (upload com controle de acesso) + RNF06 (documentos e dados bancários criptografados no banco).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(StubGeocodingPortConfig.class)
class SensitiveDataAndFilesE2ETest extends AbstractIntegrationTest {

    private static final byte[] PDF = "%PDF-1.7 documento".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3, 4};

    @LocalServerPort
    private int port;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void registrationDocumentIsPrivateAndSensitiveDataIsEncryptedAtRest() {
        RestTestClient client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        // Documento enviado antes de a conta existir (RF01).
        StoredFileResponse document = upload(client, "SUPPORTING_DOCUMENT", "cpf.pdf", PDF, null);
        assertThat(document.contentType()).isEqualTo("application/pdf");

        String email = "ana.produtora+" + UUID.randomUUID() + "@example.com";
        UUID producerId = client.post()
                .uri("/api/v1/producers")
                .body(new RegisterProducerRequest(email, "S3nhaForte!", "Ana Produtora", TaxDocumentType.CPF,
                        "12345678909", ProductionType.FARMING,
                        new AddressRequest("Rua das Flores", "100", "Centro", "Blumenau", "SC", "89010-000", null),
                        List.of(new SupportingDocumentRequest(SupportingDocumentType.CPF, "12345678909",
                                document.url()))))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProducerRegistrationResponse.class)
                .returnResult()
                .getResponseBody()
                .producerId();

        // Anônimo não lê o documento; o administrador lê.
        client.get().uri(document.url()).exchange().expectStatus().isNotFound();
        byte[] downloaded = client.get()
                .uri(document.url())
                .header("Authorization", "Bearer " + mintToken("ADMINISTRATOR"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .returnResult()
                .getResponseBody();
        assertThat(downloaded).isEqualTo(PDF);

        String producerToken = login(client, email);
        client.put()
                .uri("/api/v1/producers/me/bank-details")
                .header("Authorization", "Bearer " + producerToken)
                .body(new UpdateBankDetailsRequest("Banco do Brasil", "1234", "56789-0", BankAccountType.CHECKING,
                        "Ana Produtora"))
                .exchange()
                .expectStatus().isNoContent();

        // RNF06 — no banco, só texto cifrado; pela API, o próprio produtor vê os valores originais.
        var row = jdbcTemplate.queryForMap(
                "SELECT document_number, bank_account, bank_account_holder FROM producers WHERE id = ?", producerId);
        assertThat(row.values()).allSatisfy(value -> assertThat((String) value).startsWith("enc:v1:"));
        assertThat((String) row.get("bank_account")).doesNotContain("56789-0");
        String supportingNumber = jdbcTemplate.queryForObject(
                "SELECT document_number FROM producer_supporting_documents WHERE producer_id = ?", String.class,
                producerId);
        assertThat(supportingNumber).startsWith("enc:v1:");

        ProducerAccountResponse account = client.get()
                .uri("/api/v1/producers/me/account")
                .header("Authorization", "Bearer " + producerToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProducerAccountResponse.class)
                .returnResult()
                .getResponseBody();
        assertThat(account.bankDetails().account()).isEqualTo("56789-0");
    }

    @Test
    void offerPhotosArePublicButOnlyProducersCanUploadThem() {
        RestTestClient client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        String email = "bruno.produtor+" + UUID.randomUUID() + "@example.com";
        client.post()
                .uri("/api/v1/producers")
                .body(new RegisterProducerRequest(email, "S3nhaForte!", "Bruno Produtor", TaxDocumentType.CPF,
                        "12345678909", ProductionType.FARMING,
                        new AddressRequest("Rua das Flores", "100", "Centro", "Blumenau", "SC", "89010-000", null),
                        List.of(new SupportingDocumentRequest(SupportingDocumentType.CPF, "12345678909",
                                "https://files/doc.pdf"))))
                .exchange()
                .expectStatus().isCreated();
        String producerToken = login(client, email);

        StoredFileResponse photo = upload(client, "OFFER_PHOTO", "alface.png", PNG, producerToken);
        client.get().uri(photo.url()).exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_PNG);

        uploadExpectingStatus(client, "OFFER_PHOTO", "foto.png", PNG, mintToken("RESTAURANT"), 422);
        uploadExpectingStatus(client, "OFFER_PHOTO", "foto.png", PNG, null, 422);
        // Conteúdo que não é imagem, mesmo com extensão .png, é recusado.
        uploadExpectingStatus(client, "OFFER_PHOTO", "foto.png", "texto".getBytes(StandardCharsets.UTF_8),
                producerToken, 400);
    }

    private StoredFileResponse upload(RestTestClient client, String purpose, String name, byte[] content,
                                      String token) {
        RestTestClient.RequestBodySpec request = client.post()
                .uri("/api/v1/files")
                .contentType(MediaType.MULTIPART_FORM_DATA);
        if (token != null) {
            request = request.header("Authorization", "Bearer " + token);
        }
        return request.body(multipart(purpose, name, content))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(StoredFileResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private void uploadExpectingStatus(RestTestClient client, String purpose, String name, byte[] content,
                                       String token, int status) {
        RestTestClient.RequestBodySpec request = client.post()
                .uri("/api/v1/files")
                .contentType(MediaType.MULTIPART_FORM_DATA);
        if (token != null) {
            request = request.header("Authorization", "Bearer " + token);
        }
        request.body(multipart(purpose, name, content)).exchange().expectStatus().isEqualTo(status);
    }

    private static Object multipart(String purpose, String name, byte[] content) {
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("purpose", purpose);
        parts.add("file", new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return name;
            }
        });
        return parts;
    }

    private String login(RestTestClient client, String email) {
        return client.post()
                .uri("/api/v1/auth/login")
                .body(new LoginRequest(email, "S3nhaForte!"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .returnResult()
                .getResponseBody()
                .accessToken();
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
