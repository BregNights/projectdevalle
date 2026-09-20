package br.com.senac.projectdevalle.registration.application.restaurant.command;

import java.util.UUID;

public record RemoveDeliveryAddressCommand(UUID userId, UUID deliveryAddressId) {
}
