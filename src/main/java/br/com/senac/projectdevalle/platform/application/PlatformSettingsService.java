package br.com.senac.projectdevalle.platform.application;

import br.com.senac.projectdevalle.platform.application.command.UpdatePlatformSettingsCommand;
import br.com.senac.projectdevalle.platform.domain.PlatformSettings;
import br.com.senac.projectdevalle.platform.domain.PlatformSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// RF43 — leitura e alteração dos parâmetros globais pela administração.
@Service
public class PlatformSettingsService {

    private final PlatformSettingsRepository repository;

    public PlatformSettingsService(PlatformSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PlatformSettings current() {
        return repository.get();
    }

    @Transactional
    public PlatformSettings update(UpdatePlatformSettingsCommand command) {
        PlatformSettings settings = PlatformSettings.of(command.commissionPercentage(),
                command.cancellationPenaltyPercentage(), command.coverageRegions(),
                command.enabledProductCategories());
        return repository.save(settings);
    }
}
