package br.com.senac.projectdevalle.registration.application.admin.command;

import java.util.UUID;

public record RemoveProducerRegistrationCommand(UUID producerId, String reason) {
}
