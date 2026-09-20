package br.com.senac.projectdevalle.registration.application.restaurant.command;

import java.util.UUID;

public record SetPrimaryDeliveryAddressCommand(UUID userId, UUID deliveryAddressId) {
}
