package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.shared.domain.vo.Address;
import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String street,
        @NotBlank String number,
        @NotBlank String neighborhood,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String zipCode,
        String complement
) {

    public Address toDomain() {
        return new Address(street, number, neighborhood, city, state, zipCode, complement);
    }
}
</content>
