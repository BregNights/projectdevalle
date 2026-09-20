package br.com.senac.projectdevalle.registration.application.restaurant.command;

import br.com.senac.projectdevalle.registration.domain.restaurant.Contact;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;

import java.util.UUID;

public record UpdateRestaurantProfileCommand(
        UUID userId,
        String corporateName,
        EstablishmentCategory category,
        Contact contact
) {
}
