package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import java.util.UUID;

public record RestaurantRegistrationResponse(UUID restaurantId, UUID userId) {
}
