package br.com.senac.projectdevalle.registration.application.producer.command;

import br.com.senac.projectdevalle.registration.domain.producer.DeliveryArea;

import java.util.UUID;

public record UpdateDeliveryAreaCommand(UUID producerId, DeliveryArea deliveryArea) {
}
</content>
