package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;

public record Cpf(String value) {

    public Cpf {
        if (value == null || value.isBlank()) {
            throw new ValidationException("CPF não pode ser vazio.");
        }

        value = Digits.extract(value);

        if (!isValid(value)) {
            throw new ValidationException("CPF inválido.");
        }
    }

    static boolean isValid(String digits) {
        if (digits.length() != 11 || Digits.allEqual(digits)) {
            return false;
        }

        return checkDigit(digits, 9) == Digits.at(digits, 9)
                && checkDigit(digits, 10) == Digits.at(digits, 10);
    }

    private static int checkDigit(String cpf, int length) {
        int sum = 0;

        for (int i = 0; i < length; i++) {
            sum += Digits.at(cpf, i) * (length + 1 - i);
        }

        int remainder = (sum * 10) % 11;

        return remainder == 10 ? 0 : remainder;
    }

    public String formatted() {
        return value.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}
