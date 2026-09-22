package br.com.senac.projectdevalle.ordering.application;

import br.com.senac.projectdevalle.ordering.domain.order.Order;
import br.com.senac.projectdevalle.ordering.domain.order.OrderParty;

import java.util.UUID;

// Usuário logado visto como parte de pedidos: restaurante ou produtor, com o id do respectivo cadastro.
public record Participant(OrderParty party, UUID partyId) {

    public boolean takesPartIn(Order order) {
        return switch (party) {
            case RESTAURANT -> order.restaurantId().equals(partyId);
            case PRODUCER -> order.producerId().equals(partyId);
            case SYSTEM -> false;
        };
    }
}
