package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.restaurant.DeliveryAddress;
import br.com.senac.projectdevalle.shared.domain.vo.Address;

import java.util.UUID;

public record DeliveryAddressResponse(UUID id, String label, Address address, boolean primary,
                                       boolean geocodingPending) {

    public static DeliveryAddressResponse from(DeliveryAddress deliveryAddress) {
        return new DeliveryAddressResponse(deliveryAddress.id(), deliveryAddress.label(), deliveryAddress.address(),
                deliveryAddress.primary(), deliveryAddress.coordinates() == null);
    }
}
