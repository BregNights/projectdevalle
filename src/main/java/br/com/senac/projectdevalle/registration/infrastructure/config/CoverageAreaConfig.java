package br.com.senac.projectdevalle.registration.infrastructure.config;

import br.com.senac.projectdevalle.registration.domain.producer.CoverageAreaPolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CoverageAreaProperties.class)
public class CoverageAreaConfig {

    @Bean
    public CoverageAreaPolicy coverageAreaPolicy(CoverageAreaProperties properties) {
        return new ConfigurableCoverageAreaPolicy(properties);
    }
}
</content>
