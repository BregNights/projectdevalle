package br.com.senac.projectdevalle.shared.infrastructure.geo.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "google.maps")
public record GoogleMapsProperties(String apiKey, Duration connectTimeout, Duration readTimeout) {
}
</content>
