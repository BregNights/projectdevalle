package br.com.senac.projectdevalle.registration.infrastructure.security;

import br.com.senac.projectdevalle.registration.application.user.port.TokenServicePort;
import br.com.senac.projectdevalle.registration.domain.user.User;
import br.com.senac.projectdevalle.shared.infrastructure.config.JwtProperties;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
class JwtTokenServiceAdapter implements TokenServicePort {

    private static final String ISSUER = "projectdevalle";
    private static final String PURPOSE_CLAIM = "purpose";
    private static final String PASSWORD_RESET_PURPOSE = "password_reset";
    private static final Duration PASSWORD_RESET_EXPIRATION = Duration.ofMinutes(30);

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    JwtTokenServiceAdapter(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.expiration()))
                .subject(user.id().toString())
                .claim("role", user.role().name())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public String issuePasswordResetToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(PASSWORD_RESET_EXPIRATION))
                .subject(user.id().toString())
                .claim(PURPOSE_CLAIM, PASSWORD_RESET_PURPOSE)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public UUID parsePasswordResetSubject(String token) {
        try {
            var jwt = jwtDecoder.decode(token);
            if (!PASSWORD_RESET_PURPOSE.equals(jwt.getClaimAsString(PURPOSE_CLAIM))) {
                throw new IllegalArgumentException("Token is not a password reset token");
            }
            return UUID.fromString(jwt.getSubject());
        } catch (JwtException exception) {
            throw new IllegalArgumentException("Invalid or expired password reset token", exception);
        }
    }
}
