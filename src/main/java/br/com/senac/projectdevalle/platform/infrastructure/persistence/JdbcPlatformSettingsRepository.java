package br.com.senac.projectdevalle.platform.infrastructure.persistence;

import br.com.senac.projectdevalle.platform.domain.CoverageRegion;
import br.com.senac.projectdevalle.platform.domain.PlatformSettings;
import br.com.senac.projectdevalle.platform.domain.PlatformSettingsRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Agregado pequeno e substituído por inteiro a cada alteração: JdbcTemplate é mais direto que mapear
// três entidades JPA (linha única de configuração + regiões + municípios).
// É lido em toda aprovação e busca de catálogo, então fica em cache por alguns segundos; quem salva
// atualiza o cache na hora (outras instâncias enxergam a mudança em até CACHE_TTL).
@Component
class JdbcPlatformSettingsRepository implements PlatformSettingsRepository {

    private static final int SETTINGS_ROW_ID = 1;
    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    private final JdbcTemplate jdbcTemplate;
    private volatile CachedSettings cache;

    JdbcPlatformSettingsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PlatformSettings get() {
        CachedSettings current = cache;
        if (current != null && current.loadedAt().plus(CACHE_TTL).isAfter(Instant.now())) {
            return current.settings();
        }
        PlatformSettings loaded = load();
        cache = new CachedSettings(loaded, Instant.now());
        return loaded;
    }

    private PlatformSettings load() {
        record Row(BigDecimal commission, BigDecimal penalty, Set<String> categories) {
        }
        Row row = jdbcTemplate.queryForObject(
                "SELECT commission_percentage, cancellation_penalty_percentage, enabled_product_categories "
                        + "FROM platform_settings WHERE id = ?",
                (resultSet, rowNumber) -> new Row(resultSet.getBigDecimal("commission_percentage"),
                        resultSet.getBigDecimal("cancellation_penalty_percentage"),
                        toSet(resultSet.getArray("enabled_product_categories"))),
                SETTINGS_ROW_ID);
        return PlatformSettings.of(row.commission(), row.penalty(), loadRegions(), row.categories());
    }

    @Override
    public PlatformSettings save(PlatformSettings settings) {
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement("UPDATE platform_settings SET commission_percentage = ?, "
                    + "cancellation_penalty_percentage = ?, enabled_product_categories = ?, updated_at = now() "
                    + "WHERE id = ?");
            statement.setBigDecimal(1, settings.commissionPercentage());
            statement.setBigDecimal(2, settings.cancellationPenaltyPercentage());
            statement.setArray(3, connection.createArrayOf("text",
                    settings.enabledProductCategories().stream().sorted().toArray()));
            statement.setInt(4, SETTINGS_ROW_ID);
            return statement;
        });
        jdbcTemplate.update("DELETE FROM platform_coverage_cities");
        List<Object[]> rows = new ArrayList<>();
        int regionPosition = 0;
        for (CoverageRegion region : settings.coverageRegions()) {
            int cityPosition = 0;
            for (String city : region.cities()) {
                rows.add(new Object[]{region.name(), regionPosition, city, cityPosition++});
            }
            regionPosition++;
        }
        jdbcTemplate.batchUpdate("INSERT INTO platform_coverage_cities (region, region_position, city, city_position) "
                + "VALUES (?, ?, ?, ?)", rows);
        PlatformSettings saved = load();
        cache = new CachedSettings(saved, Instant.now());
        return saved;
    }

    private List<CoverageRegion> loadRegions() {
        Map<String, List<String>> citiesByRegion = new LinkedHashMap<>();
        jdbcTemplate.query("SELECT region, city FROM platform_coverage_cities ORDER BY region_position, city_position",
                resultSet -> {
                    citiesByRegion.computeIfAbsent(resultSet.getString("region"), key -> new ArrayList<>())
                            .add(resultSet.getString("city"));
                });
        return citiesByRegion.entrySet().stream()
                .map(entry -> new CoverageRegion(entry.getKey(), entry.getValue()))
                .toList();
    }

    private record CachedSettings(PlatformSettings settings, Instant loadedAt) {
    }

    private static Set<String> toSet(Array array) throws SQLException {
        if (array == null) {
            return Set.of();
        }
        return new LinkedHashSet<>(Arrays.asList((String[]) array.getArray()));
    }
}
