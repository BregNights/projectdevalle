package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.producer.BankAccountType;
import br.com.senac.projectdevalle.registration.domain.producer.BankDetails;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateBankDetailsRequest(
        @NotBlank String bankName,
        @NotBlank String agency,
        @NotBlank String account,
        @NotNull BankAccountType accountType,
        @NotBlank String accountHolder
) {

    public BankDetails toDomain() {
        return new BankDetails(bankName, agency, account, accountType, accountHolder);
    }
}
