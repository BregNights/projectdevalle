package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRestaurantRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String corporateName,
        @NotBlank String cnpj,
        @NotNull EstablishmentCategory category,
        @NotNull @Valid ContactRequest contact,
        @NotBlank String initialAddressLabel,
        @NotNull @Valid AddressRequest initialAddress
) {
}
