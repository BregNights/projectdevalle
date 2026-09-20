package br.com.senac.projectdevalle.registration.infrastructure.security;

import br.com.senac.projectdevalle.registration.application.user.port.GoogleIdTokenVerifierPort;
import br.com.senac.projectdevalle.registration.application.user.port.SocialIdentity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class GoogleIdTokenVerifierAdapter implements GoogleIdTokenVerifierPort {

    private static final String JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> VALID_ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final JwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();
    private final String clientId;

    GoogleIdTokenVerifierAdapter(@Value("${security.oauth2.google.client-id}") String clientId) {
        this.clientId = clientId;
    }

    @Override
    public SocialIdentity verify(String idToken) {
        try {
            Jwt jwt = jwtDecoder.decode(idToken);

            if (!VALID_ISSUERS.contains(jwt.getIssuer().toString())) {
                throw new IllegalArgumentException("Unexpected Google issuer");
            }
            if (!jwt.getAudience().contains(clientId)) {
                throw new IllegalArgumentException("Unexpected Google audience");
            }

            boolean emailVerified = Boolean.TRUE.equals(jwt.getClaim("email_verified"))
                    || "true".equals(jwt.getClaimAsString("email_verified"));
            return new SocialIdentity(jwt.getClaimAsString("email"), emailVerified, jwt.getClaimAsString("name"));
        } catch (JwtException exception) {
            throw new IllegalArgumentException("Invalid Google ID token", exception);
        }
    }
}
