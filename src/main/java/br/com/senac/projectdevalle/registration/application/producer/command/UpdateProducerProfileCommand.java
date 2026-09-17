package br.com.senac.projectdevalle.registration.application.producer.command;

import java.util.UUID;

public record UpdateProducerProfileCommand(UUID producerId, String name) {
}
