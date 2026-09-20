package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.restaurant.Contact;

public record ContactResponse(String name, String role, String phone, String email) {

    public static ContactResponse from(Contact contact) {
        return new ContactResponse(contact.name(), contact.role(), contact.phone(), contact.email());
    }
}
