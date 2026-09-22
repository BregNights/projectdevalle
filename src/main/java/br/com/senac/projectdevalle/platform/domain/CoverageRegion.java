package br.com.senac.projectdevalle.platform.domain;

import br.com.senac.projectdevalle.shared.domain.PlaceNames;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// RF43/RN02 — região atendida (ex.: "Vale do Itajaí") e seus municípios.
public record CoverageRegion(String name, List<String> cities) {

    public CoverageRegion {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("region name must not be blank");
        }
        name = name.trim();
        List<String> cleaned = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String city : cities == null ? List.<String>of() : cities) {
            if (city != null && !city.isBlank() && seen.add(PlaceNames.normalize(city))) {
                cleaned.add(city.trim().replaceAll("\\s+", " "));
            }
        }
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("region " + name + " must have at least one city");
        }
        cities = List.copyOf(cleaned);
    }

    public boolean contains(String city) {
        return cities.stream().anyMatch(candidate -> PlaceNames.sameName(candidate, city));
    }
}
