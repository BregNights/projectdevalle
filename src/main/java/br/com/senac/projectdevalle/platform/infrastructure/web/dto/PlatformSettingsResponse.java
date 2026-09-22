package br.com.senac.projectdevalle.platform.infrastructure.web.dto;

import br.com.senac.projectdevalle.platform.domain.PlatformSettings;

import java.math.BigDecimal;
import java.util.List;

public record PlatformSettingsResponse(BigDecimal commissionPercentage, BigDecimal cancellationPenaltyPercentage,
                                       List<CoverageRegionPayload> coverageRegions,
                                       List<String> enabledProductCategories) {

    public static PlatformSettingsResponse from(PlatformSettings settings) {
        return new PlatformSettingsResponse(settings.commissionPercentage(), settings.cancellationPenaltyPercentage(),
                settings.coverageRegions().stream().map(CoverageRegionPayload::from).toList(),
                settings.enabledProductCategories().stream().sorted().toList());
    }
}
