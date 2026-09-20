package br.com.senac.projectdevalle.registration.application.producer.command;

import br.com.senac.projectdevalle.registration.domain.producer.DeliveryArea;

import java.util.UUID;

public record UpdateDeliveryAreaCommand(UUID userId, DeliveryArea deliveryArea) {
}
