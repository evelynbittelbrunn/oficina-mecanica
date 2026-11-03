package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;

public record Cnpj(String value) {

    private static final int[] FIRST_DIGIT_WEIGHTS = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] SECOND_DIGIT_WEIGHTS = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Cnpj {
        if (value == null || value.isBlank()) {
            throw new ValidationException("CNPJ não pode ser vazio.");
        }

        value = Digits.extract(value);

        if (!isValid(value)) {
            throw new ValidationException("CNPJ inválido.");
        }
    }

    static boolean isValid(String digits) {
        if (digits.length() != 14 || Digits.allEqual(digits)) {
            return false;
        }

        return checkDigit(digits, FIRST_DIGIT_WEIGHTS) == Digits.at(digits, 12)
                && checkDigit(digits, SECOND_DIGIT_WEIGHTS) == Digits.at(digits, 13);
    }

    private static int checkDigit(String cnpj, int[] weights) {
        int sum = 0;

        for (int i = 0; i < weights.length; i++) {
            sum += Digits.at(cnpj, i) * weights[i];
        }

        int remainder = sum % 11;

        return remainder < 2 ? 0 : 11 - remainder;
    }

    public String formatted() {
        return value.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }
}
