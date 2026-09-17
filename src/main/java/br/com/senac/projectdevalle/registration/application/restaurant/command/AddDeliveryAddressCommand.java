package br.com.senac.projectdevalle.registration.application.restaurant.command;

import br.com.senac.projectdevalle.shared.domain.vo.Address;

import java.util.UUID;

public record AddDeliveryAddressCommand(UUID restaurantId, String label, Address address, boolean primary) {
}
</content>
