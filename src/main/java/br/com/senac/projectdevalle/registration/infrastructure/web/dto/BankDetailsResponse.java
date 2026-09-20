package br.com.senac.projectdevalle.registration.infrastructure.web.dto;

import br.com.senac.projectdevalle.registration.domain.producer.BankAccountType;
import br.com.senac.projectdevalle.registration.domain.producer.BankDetails;

public record BankDetailsResponse(
        String bankName,
        String agency,
        String account,
        BankAccountType accountType,
        String accountHolder
) {

    public static BankDetailsResponse from(BankDetails bankDetails) {
        if (bankDetails == null) {
            return null;
        }
        return new BankDetailsResponse(bankDetails.bankName(), bankDetails.agency(), bankDetails.account(),
                bankDetails.accountType(), bankDetails.accountHolder());
    }
}
