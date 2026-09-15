package br.com.senac.projectdevalle.shared.domain.vo;

public record Cnpj(String digits) implements TaxDocument {

    private static final int[] FIRST_CHECK_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] SECOND_CHECK_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Cnpj {
        digits = digits == null ? null : digits.replaceAll("\\D", "");
        if (digits == null || digits.length() != 14 || allDigitsEqual(digits) || !hasValidCheckDigits(digits)) {
            throw new IllegalArgumentException("Invalid CNPJ: " + digits);
        }
    }

    private static boolean allDigitsEqual(String digits) {
        return digits.chars().distinct().count() == 1;
    }

    private static boolean hasValidCheckDigits(String digits) {
        int firstCheckDigit = calculateCheckDigit(digits.substring(0, 12), FIRST_CHECK_WEIGHTS);
        int secondCheckDigit = calculateCheckDigit(digits.substring(0, 12) + firstCheckDigit, SECOND_CHECK_WEIGHTS);
        return digits.equals(digits.substring(0, 12) + firstCheckDigit + secondCheckDigit);
    }

    private static int calculateCheckDigit(String base, int[] weights) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            sum += (base.charAt(i) - '0') * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
</content>
