package br.com.senac.projectdevalle.catalog.infrastructure.platform;

import br.com.senac.projectdevalle.catalog.application.port.CatalogSettingsPort;
import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.platform.domain.PlatformSettingsRepository;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Component
class CatalogSettingsAdapter implements CatalogSettingsPort {

    private final PlatformSettingsRepository platformSettingsRepository;

    CatalogSettingsAdapter(PlatformSettingsRepository platformSettingsRepository) {
        this.platformSettingsRepository = platformSettingsRepository;
    }

    @Override
    public Set<ProductCategory> enabledCategories() {
        Set<String> enabled = platformSettingsRepository.get().enabledProductCategories();
        Set<ProductCategory> result = EnumSet.noneOf(ProductCategory.class);
        for (ProductCategory category : ProductCategory.values()) {
            if (enabled.contains(category.name())) {
                result.add(category);
            }
        }
        return result;
    }

    @Override
    public Optional<Set<String>> citiesOfRegion(String region) {
        return platformSettingsRepository.get().region(region)
                .map(coverageRegion -> new LinkedHashSet<>(coverageRegion.cities()));
    }
}
