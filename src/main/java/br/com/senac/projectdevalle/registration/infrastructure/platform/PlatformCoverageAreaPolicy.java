package br.com.senac.projectdevalle.registration.infrastructure.platform;

import br.com.senac.projectdevalle.platform.domain.PlatformSettingsRepository;
import br.com.senac.projectdevalle.registration.domain.producer.CoverageAreaPolicy;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import org.springframework.stereotype.Component;

// RN02/RF43 — a área de cobertura vem da configuração da plataforma, editável pela administração.
@Component
class PlatformCoverageAreaPolicy implements CoverageAreaPolicy {

    private final PlatformSettingsRepository platformSettingsRepository;

    PlatformCoverageAreaPolicy(PlatformSettingsRepository platformSettingsRepository) {
        this.platformSettingsRepository = platformSettingsRepository;
    }

    @Override
    public boolean covers(Address address) {
        return platformSettingsRepository.get().covers(address.city());
    }
}
