package br.com.senac.projectdevalle.platform.application.command;

import br.com.senac.projectdevalle.platform.domain.CoverageRegion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record UpdatePlatformSettingsCommand(BigDecimal commissionPercentage, List<CoverageRegion> coverageRegions,
                                            Set<String> enabledProductCategories) {
}
