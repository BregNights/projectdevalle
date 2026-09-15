package br.com.senac.projectdevalle.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

// privateKey/publicKey são strings Base64 do DER (PKCS8 para a chave privada, X509 para a publica).
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String privateKey, String publicKey, Duration expiration) {
}
</content>
