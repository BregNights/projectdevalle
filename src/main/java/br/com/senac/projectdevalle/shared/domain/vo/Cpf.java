package br.com.senac.projectdevalle.shared.domain.vo;

public record Cpf(String digits) implements TaxDocument {

    public Cpf {
        digits = digits == null ? null : digits.replaceAll("\\D", "");
        if (digits == null || digits.length() != 11 || allDigitsEqual(digits) || !hasValidCheckDigits(digits)) {
            throw new IllegalArgumentException("Invalid CPF: " + digits);
        }
    }

    private static boolean allDigitsEqual(String digits) {
        return digits.chars().distinct().count() == 1;
    }

    private static boolean hasValidCheckDigits(String digits) {
        int firstCheckDigit = calculateCheckDigit(digits.substring(0, 9), 10);
        int secondCheckDigit = calculateCheckDigit(digits.substring(0, 9) + firstCheckDigit, 11);
        return digits.equals(digits.substring(0, 9) + firstCheckDigit + secondCheckDigit);
    }

    private static int calculateCheckDigit(String base, int firstWeight) {
        int sum = 0;
        int weight = firstWeight;
        for (char digit : base.toCharArray()) {
            sum += (digit - '0') * weight--;
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
