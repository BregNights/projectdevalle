package br.com.senac.projectdevalle.registration.domain.producer;

public record BankDetails(
        String bankName,
        String agency,
        String account,
        BankAccountType accountType,
        String accountHolder
) {

    public BankDetails {
        requireNonBlank(bankName, "bankName");
        requireNonBlank(agency, "agency");
        requireNonBlank(account, "account");
        requireNonBlank(accountHolder, "accountHolder");
        if (accountType == null) {
            throw new IllegalArgumentException("accountType must not be null");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
