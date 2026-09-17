package br.com.senac.projectdevalle.registration.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// RF43 — regiões atendidas pela plataforma, configuráveis pela administração.
// Nesta fase é uma lista estática via properties; um painel de admin poderá substituí-la no futuro.
@ConfigurationProperties(prefix = "app.registration.coverage-area")
public record CoverageAreaProperties(List<String> cities) {

    public CoverageAreaProperties {
        cities = cities == null ? List.of() : List.copyOf(cities);
    }
}
