package br.com.senac.projectdevalle.platform.infrastructure.web;

import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import br.com.senac.projectdevalle.platform.application.PlatformSettingsService;
import br.com.senac.projectdevalle.platform.application.command.UpdatePlatformSettingsCommand;
import br.com.senac.projectdevalle.platform.domain.PlatformSettings;
import br.com.senac.projectdevalle.platform.infrastructure.web.dto.CoverageRegionPayload;
import br.com.senac.projectdevalle.platform.infrastructure.web.dto.PlatformSettingsResponse;
import br.com.senac.projectdevalle.platform.infrastructure.web.dto.UpdatePlatformSettingsRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class PlatformSettingsController {

    private final PlatformSettingsService platformSettingsService;

    public PlatformSettingsController(PlatformSettingsService platformSettingsService) {
        this.platformSettingsService = platformSettingsService;
    }

    // RF43 — configuração completa, restrita à administração.
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @GetMapping("/api/v1/admin/settings")
    public PlatformSettingsResponse current() {
        return PlatformSettingsResponse.from(platformSettingsService.current());
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PutMapping("/api/v1/admin/settings")
    public PlatformSettingsResponse update(@Valid @RequestBody UpdatePlatformSettingsRequest request) {
        PlatformSettings updated = platformSettingsService.update(new UpdatePlatformSettingsCommand(
                request.commissionPercentage(),
                request.coverageRegions().stream().map(CoverageRegionPayload::toDomain).toList(),
                request.enabledProductCategories().stream().map(ProductCategory::name).collect(Collectors.toSet())));
        return PlatformSettingsResponse.from(updated);
    }

    // Informação pública para as telas de cadastro, catálogo e ofertas: regiões/municípios atendidos e
    // categorias habilitadas. A comissão fica fora (é exibida por pedido, quando pedidos existirem).
    @GetMapping("/api/v1/platform/coverage")
    public PublicPlatformInfo publicInfo() {
        PlatformSettings settings = platformSettingsService.current();
        return new PublicPlatformInfo(
                settings.coverageRegions().stream().map(CoverageRegionPayload::from).toList(),
                settings.enabledProductCategories().stream().sorted().toList());
    }

    public record PublicPlatformInfo(List<CoverageRegionPayload> coverageRegions,
                                     List<String> enabledProductCategories) {
    }
}
