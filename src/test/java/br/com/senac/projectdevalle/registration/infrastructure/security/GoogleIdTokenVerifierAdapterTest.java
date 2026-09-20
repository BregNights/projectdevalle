package br.com.senac.projectdevalle.registration.infrastructure.security;

import br.com.senac.projectdevalle.registration.application.user.port.SocialIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleIdTokenVerifierAdapterTest {

    private static final String CLIENT_ID = "expected-client-id";

    @Mock
    private JwtDecoder jwtDecoder;

    private GoogleIdTokenVerifierAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GoogleIdTokenVerifierAdapter(jwtDecoder, CLIENT_ID);
    }

    @Test
    void extractsVerifiedIdentityFromValidToken() {
        when(jwtDecoder.decode(any())).thenReturn(googleJwt("https://accounts.google.com", CLIENT_ID,
                Map.of("email", "joao@example.com", "email_verified", true, "name", "Joao")));

        SocialIdentity identity = adapter.verify("token");

        assertThat(identity.email()).isEqualTo("joao@example.com");
        assertThat(identity.emailVerified()).isTrue();
        assertThat(identity.displayName()).isEqualTo("Joao");
    }

    @Test
    void acceptsBareIssuerHostWithoutScheme() {
        when(jwtDecoder.decode(any())).thenReturn(googleJwt("accounts.google.com", CLIENT_ID,
                Map.of("email", "joao@example.com", "email_verified", true)));

        assertThat(adapter.verify("token").email()).isEqualTo("joao@example.com");
    }

    @Test
    void acceptsEmailVerifiedAsStringClaim() {
        when(jwtDecoder.decode(any())).thenReturn(googleJwt("https://accounts.google.com", CLIENT_ID,
                Map.of("email", "joao@example.com", "email_verified", "true")));

        assertThat(adapter.verify("token").emailVerified()).isTrue();
    }

    @Test
    void reportsUnverifiedEmailWhenClaimIsFalse() {
        when(jwtDecoder.decode(any())).thenReturn(googleJwt("https://accounts.google.com", CLIENT_ID,
                Map.of("email", "joao@example.com", "email_verified", false)));

        assertThat(adapter.verify("token").emailVerified()).isFalse();
    }

    @Test
    void rejectsTokenWithUnexpectedIssuer() {
        when(jwtDecoder.decode(any())).thenReturn(googleJwt("https://evil.example.com", CLIENT_ID,
                Map.of("email", "joao@example.com", "email_verified", true)));

        assertThatThrownBy(() -> adapter.verify("token")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsTokenIssuedForAnotherAudience() {
        when(jwtDecoder.decode(any())).thenReturn(googleJwt("https://accounts.google.com", "someone-elses-client-id",
                Map.of("email", "joao@example.com", "email_verified", true)));

        assertThatThrownBy(() -> adapter.verify("token")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void wrapsDecodingFailureAsIllegalArgument() {
        when(jwtDecoder.decode(any())).thenThrow(new BadJwtException("invalid signature"));

        assertThatThrownBy(() -> adapter.verify("token")).isInstanceOf(IllegalArgumentException.class);
    }

    private static Jwt googleJwt(String issuer, String audience, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .issuer(issuer)
                .audience(List.of(audience))
                .claims(claims -> claims.putAll(extraClaims))
                .build();
    }
}
