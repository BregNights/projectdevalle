package br.com.senac.projectdevalle.platform.infrastructure.web.dto;

import br.com.senac.projectdevalle.platform.domain.CoverageRegion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CoverageRegionPayload(@NotBlank @Size(max = 120) String name,
                                    @NotEmpty List<@NotBlank @Size(max = 120) String> cities) {

    public static CoverageRegionPayload from(CoverageRegion region) {
        return new CoverageRegionPayload(region.name(), region.cities());
    }

    public CoverageRegion toDomain() {
        return new CoverageRegion(name, cities);
    }
}
