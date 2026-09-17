package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.restaurant.Contact;
import jakarta.validation.constraints.NotBlank;

public record ContactRequest(@NotBlank String name, String role, String phone, String email) {

    public Contact toDomain() {
        return new Contact(name, role, phone, email);
    }
}
