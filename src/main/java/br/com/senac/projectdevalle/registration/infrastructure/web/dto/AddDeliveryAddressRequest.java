package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddDeliveryAddressRequest(
        @NotBlank String label,
        @NotNull @Valid AddressRequest address,
        boolean primary
) {
}
</content>
