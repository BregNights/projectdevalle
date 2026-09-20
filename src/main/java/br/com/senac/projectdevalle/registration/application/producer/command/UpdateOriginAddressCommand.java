package br.com.senac.projectdevalle.registration.application.producer.command;

import br.com.senac.projectdevalle.shared.domain.vo.Address;

import java.util.UUID;

public record UpdateOriginAddressCommand(UUID userId, Address address) {
}
