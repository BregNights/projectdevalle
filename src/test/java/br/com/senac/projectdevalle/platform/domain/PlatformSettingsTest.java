package br.com.senac.projectdevalle.platform.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlatformSettingsTest {

    private static final List<CoverageRegion> REGIONS = List.of(
            new CoverageRegion("Vale do Itajaí", List.of("Blumenau", "Gaspar")),
            new CoverageRegion("Litoral Norte", List.of("Itajaí", "Balneário Camboriú")));

    // RN02 — a comparação de municípios ignora acento, caixa e espaços extras.
    @Test
    void coversCitiesIgnoringAccentsAndCase() {
        PlatformSettings settings = PlatformSettings.of(BigDecimal.TEN, REGIONS, Set.of("FISH"));

        assertThat(settings.covers("itajai")).isTrue();
        assertThat(settings.covers("  BALNEARIO   camboriu ")).isTrue();
        assertThat(settings.covers("Curitiba")).isFalse();
        assertThat(settings.region("vale do itajai")).isPresent();
    }

    @Test
    void validatesCommissionRange() {
        assertThatThrownBy(() -> PlatformSettings.of(BigDecimal.valueOf(-1), REGIONS, Set.of("FISH")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PlatformSettings.of(BigDecimal.valueOf(100), REGIONS, Set.of("FISH")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PlatformSettings.of(new BigDecimal("10.555"), REGIONS, Set.of("FISH")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(PlatformSettings.of(new BigDecimal("12.50"), REGIONS, Set.of("FISH")).commissionPercentage())
                .isEqualByComparingTo("12.5");
    }

    @Test
    void rejectsACityInTwoRegionsAndDuplicatedRegions() {
        List<CoverageRegion> overlapping = List.of(
                new CoverageRegion("A", List.of("Blumenau")),
                new CoverageRegion("B", List.of("blumenau")));
        List<CoverageRegion> duplicated = List.of(
                new CoverageRegion("Vale", List.of("Blumenau")),
                new CoverageRegion("vale", List.of("Gaspar")));

        assertThatThrownBy(() -> PlatformSettings.of(BigDecimal.TEN, overlapping, Set.of("FISH")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PlatformSettings.of(BigDecimal.TEN, duplicated, Set.of("FISH")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresAtLeastOneRegionCityAndCategory() {
        assertThatThrownBy(() -> PlatformSettings.of(BigDecimal.TEN, List.of(), Set.of("FISH")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CoverageRegion("Vazia", List.of(" ")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PlatformSettings.of(BigDecimal.TEN, REGIONS, Set.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void regionDropsDuplicatedAndBlankCities() {
        CoverageRegion region = new CoverageRegion(" Vale ", List.of("Blumenau", "blumenau", " ", "Gaspar"));

        assertThat(region.name()).isEqualTo("Vale");
        assertThat(region.cities()).containsExactly("Blumenau", "Gaspar");
    }
}
