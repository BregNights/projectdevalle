package br.com.senac.projectdevalle.registration.infrastructure.config;

import br.com.senac.projectdevalle.registration.domain.producer.CoverageAreaPolicy;
import br.com.senac.projectdevalle.shared.domain.vo.Address;

class ConfigurableCoverageAreaPolicy implements CoverageAreaPolicy {

    private final CoverageAreaProperties properties;

    ConfigurableCoverageAreaPolicy(CoverageAreaProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean covers(Address address) {
        return properties.cities().stream()
                .anyMatch(city -> city.equalsIgnoreCase(address.city()));
    }
}
