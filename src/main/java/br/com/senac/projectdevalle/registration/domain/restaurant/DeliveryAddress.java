package br.com.senac.projectdevalle.registration.domain.restaurant;

import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;

import java.util.UUID;

// RF30.2 — restaurante pode ter múltiplos endereços de entrega (filiais); um deles é o principal.
public record DeliveryAddress(UUID id, String label, Address address, Coordinates coordinates, boolean primary) {

    public static DeliveryAddress add(String label, Address address, Coordinates coordinates, boolean primary) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }
        return new DeliveryAddress(UUID.randomUUID(), label, address, coordinates, primary);
    }

    public DeliveryAddress withPrimary(boolean newPrimary) {
        return new DeliveryAddress(id, label, address, coordinates, newPrimary);
    }
}
