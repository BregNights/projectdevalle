package br.com.senac.projectdevalle.ordering.infrastructure.platform;

import br.com.senac.projectdevalle.ordering.application.port.OrderingSettingsPort;
import br.com.senac.projectdevalle.platform.domain.PlatformSettingsRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
class OrderingSettingsAdapter implements OrderingSettingsPort {

    private final PlatformSettingsRepository platformSettingsRepository;

    OrderingSettingsAdapter(PlatformSettingsRepository platformSettingsRepository) {
        this.platformSettingsRepository = platformSettingsRepository;
    }

    @Override
    public BigDecimal cancellationPenaltyPercentage() {
        return platformSettingsRepository.get().cancellationPenaltyPercentage();
    }
}
