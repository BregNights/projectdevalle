package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateRestaurantProfileRequest(
        @NotBlank String corporateName,
        @NotNull EstablishmentCategory category,
        @NotNull @Valid ContactRequest contact
) {
}
</content>
