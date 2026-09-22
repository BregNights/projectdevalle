package br.com.senac.projectdevalle.platform.infrastructure.web.dto;

import br.com.senac.projectdevalle.catalog.domain.offer.ProductCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

// As categorias chegam como o enum do catálogo para que só categorias existentes possam ser habilitadas.
public record UpdatePlatformSettingsRequest(
        @NotNull @DecimalMin("0") @DecimalMax(value = "100", inclusive = false) BigDecimal commissionPercentage,
        @NotNull @DecimalMin("0") @DecimalMax(value = "100", inclusive = false) BigDecimal cancellationPenaltyPercentage,
        @NotEmpty List<@Valid CoverageRegionPayload> coverageRegions,
        @NotEmpty Set<ProductCategory> enabledProductCategories
) {
}
