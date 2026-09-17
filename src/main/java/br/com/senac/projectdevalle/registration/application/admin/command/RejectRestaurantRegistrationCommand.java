package br.com.senac.projectdevalle.registration.application.admin.command;

import java.util.UUID;

public record RejectRestaurantRegistrationCommand(UUID restaurantId, String reason) {
}
</content>
