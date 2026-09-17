package br.com.senac.projectdevalle.registration.application.producer.command;

import br.com.senac.projectdevalle.registration.domain.producer.BankDetails;

import java.util.UUID;

public record UpdateBankDetailsCommand(UUID producerId, BankDetails bankDetails) {
}
</content>
