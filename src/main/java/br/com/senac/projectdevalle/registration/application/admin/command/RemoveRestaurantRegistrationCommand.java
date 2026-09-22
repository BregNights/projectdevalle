package br.com.senac.projectdevalle.registration.application.admin.command;

import java.util.UUID;

public record RemoveRestaurantRegistrationCommand(UUID restaurantId, String reason) {
}
