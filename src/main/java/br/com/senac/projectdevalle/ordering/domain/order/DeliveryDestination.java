package br.com.senac.projectdevalle.ordering.domain.order;

import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;

import java.util.UUID;

// RF30.2 — endereço de entrega escolhido pelo restaurante no pedido. É uma cópia (snapshot): alterar ou remover
// o endereço no cadastro depois não muda pedidos já feitos.
public record DeliveryDestination(UUID addressId, String label, Address address, Coordinates coordinates) {

    public DeliveryDestination {
        if (address == null) {
            throw new IllegalArgumentException("delivery address must not be null");
        }
    }
}
