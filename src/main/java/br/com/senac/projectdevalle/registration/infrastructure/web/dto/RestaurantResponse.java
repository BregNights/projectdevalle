package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.common.RegistrationStatus;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import br.com.senac.projectdevalle.registration.domain.restaurant.Restaurant;

import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        String corporateName,
        EstablishmentCategory category,
        RegistrationStatus status,
        String statusReason,
        int deliveryAddressCount
) {

    // Visão do administrador e do próprio restaurante: inclui o motivo da rejeição/suspensão/remoção (RN29).
    public static RestaurantResponse from(Restaurant restaurant) {
        return build(restaurant, restaurant.statusReason());
    }

    // Visão de terceiros (RN37): o motivo é informação interna entre a administração e o restaurante.
    public static RestaurantResponse publicView(Restaurant restaurant) {
        return build(restaurant, null);
    }

    private static RestaurantResponse build(Restaurant restaurant, String statusReason) {
        return new RestaurantResponse(restaurant.id(), restaurant.corporateName(), restaurant.category(),
                restaurant.status(), statusReason, restaurant.deliveryAddresses().size());
    }
}
