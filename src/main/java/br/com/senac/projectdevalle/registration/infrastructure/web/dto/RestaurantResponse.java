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
        int deliveryAddressCount
) {

    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(restaurant.id(), restaurant.corporateName(), restaurant.category(),
                restaurant.status(), restaurant.deliveryAddresses().size());
    }
}
</content>
