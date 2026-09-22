package br.com.senac.projectdevalle.platform.domain;

import br.com.senac.projectdevalle.shared.domain.PlaceNames;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// RF43 — parâmetros globais da plataforma, configuráveis pela administração:
// comissão (RN13), multa de cancelamento (RN10), regiões/municípios atendidos (RN02) e categorias habilitadas.
public final class PlatformSettings {

    private static final BigDecimal MAX_PERCENTAGE = BigDecimal.valueOf(100);

    private final BigDecimal commissionPercentage;
    private final BigDecimal cancellationPenaltyPercentage;
    private final List<CoverageRegion> coverageRegions;
    private final Set<String> enabledProductCategories;

    private PlatformSettings(BigDecimal commissionPercentage, BigDecimal cancellationPenaltyPercentage,
                             List<CoverageRegion> coverageRegions, Set<String> enabledProductCategories) {
        this.commissionPercentage = commissionPercentage;
        this.cancellationPenaltyPercentage = cancellationPenaltyPercentage;
        this.coverageRegions = coverageRegions;
        this.enabledProductCategories = enabledProductCategories;
    }

    public static PlatformSettings of(BigDecimal commissionPercentage, BigDecimal cancellationPenaltyPercentage,
                                      List<CoverageRegion> coverageRegions, Set<String> enabledProductCategories) {
        requirePercentage(commissionPercentage, "commissionPercentage");
        // RN10 — a multa é proporcional ao valor do pedido; 100% significaria cobrar o pedido inteiro.
        requirePercentage(cancellationPenaltyPercentage, "cancellationPenaltyPercentage");
        if (coverageRegions == null || coverageRegions.isEmpty()) {
            throw new IllegalArgumentException("at least one coverage region is required");
        }
        Set<String> regionNames = new HashSet<>();
        Set<String> cities = new HashSet<>();
        for (CoverageRegion region : coverageRegions) {
            if (!regionNames.add(PlaceNames.normalize(region.name()))) {
                throw new IllegalArgumentException("duplicated region: " + region.name());
            }
            for (String city : region.cities()) {
                if (!cities.add(PlaceNames.normalize(city))) {
                    throw new IllegalArgumentException("city listed in more than one region: " + city);
                }
            }
        }
        if (enabledProductCategories == null || enabledProductCategories.isEmpty()) {
            throw new IllegalArgumentException("at least one product category must be enabled");
        }
        return new PlatformSettings(commissionPercentage, cancellationPenaltyPercentage, List.copyOf(coverageRegions),
                Set.copyOf(new LinkedHashSet<>(enabledProductCategories)));
    }

    private static void requirePercentage(BigDecimal value, String name) {
        if (value == null || value.signum() < 0 || value.compareTo(MAX_PERCENTAGE) >= 0) {
            throw new IllegalArgumentException(name + " must be between 0 and 100 (exclusive)");
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(name + " must have at most 2 decimal places");
        }
    }

    // RN02 — município está dentro da área atendida?
    public boolean covers(String city) {
        return coverageRegions.stream().anyMatch(region -> region.contains(city));
    }

    // RF10 — municípios de uma região, para o filtro "região" do catálogo.
    public Optional<CoverageRegion> region(String name) {
        return coverageRegions.stream().filter(region -> PlaceNames.sameName(region.name(), name)).findFirst();
    }

    public boolean isProductCategoryEnabled(String category) {
        return enabledProductCategories.contains(category);
    }

    public BigDecimal commissionPercentage() {
        return commissionPercentage;
    }

    public BigDecimal cancellationPenaltyPercentage() {
        return cancellationPenaltyPercentage;
    }

    public List<CoverageRegion> coverageRegions() {
        return coverageRegions;
    }

    public Set<String> enabledProductCategories() {
        return enabledProductCategories;
    }
}
