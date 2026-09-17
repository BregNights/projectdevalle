package br.com.senac.projectdevalle.support;

import br.com.senac.projectdevalle.shared.application.port.GeocodingPort;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// Substitui o adapter real do Google Maps nos testes de integração/e2e — nunca depende de
// rede ou de uma API key real. As coordenadas ficam dentro da área de cobertura de teste (Blumenau/SC).
@TestConfiguration
public class StubGeocodingPortConfig {

    @Bean
    @Primary
    public GeocodingPort geocodingPort() {
        return address -> new Coordinates(-26.9194, -49.0661);
    }
}
</content>
