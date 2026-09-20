package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpdateOriginAddressRequest(@NotNull @Valid AddressRequest address) {
}
