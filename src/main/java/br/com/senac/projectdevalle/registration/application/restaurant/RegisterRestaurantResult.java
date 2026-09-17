package br.com.senac.projectdevalle.registration.application.restaurant;

import java.util.UUID;

public record RegisterRestaurantResult(UUID restaurantId, UUID userId) {
}
