package br.com.oficina.shared.domain.valueobject;

import java.util.regex.Pattern;

public record Document(String value) {

    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    public Document {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Documento não pode ser vazio.");
        }

        value = NON_DIGIT.matcher(value).replaceAll("");

        if (!isValid(value)) {
            throw new IllegalArgumentException("CPF ou CNPJ inválido.");
        }
    }

    private static boolean isValid(String document) {
        if (document.length() == 11) {
            return isValidCpf(document);
        }

        if (document.length() == 14) {
            return isValidCnpj(document);
        }

        return false;
    }

    private static boolean isValidCpf(String cpf) {
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int firstDigit = calculateCpfDigit(cpf, 9);
        int secondDigit = calculateCpfDigit(cpf, 10);

        return firstDigit == Character.getNumericValue(cpf.charAt(9))
                && secondDigit == Character.getNumericValue(cpf.charAt(10));
    }

    private static int calculateCpfDigit(String cpf, int length) {
        int sum = 0;

        for (int i = 0; i < length; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (length + 1 - i);
        }

        int remainder = (sum * 10) % 11;

        return remainder == 10 ? 0 : remainder;
    }

    private static boolean isValidCnpj(String cnpj) {
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        int firstDigit = calculateCnpjDigit(cnpj.substring(0, 12));
        int secondDigit = calculateCnpjDigit(cnpj.substring(0, 12) + firstDigit);

        return firstDigit == Character.getNumericValue(cnpj.charAt(12))
                && secondDigit == Character.getNumericValue(cnpj.charAt(13));
    }

    private static int calculateCnpjDigit(String value) {
        int[] weights = value.length() == 12
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;

        for (int i = 0; i < value.length(); i++) {
            sum += Character.getNumericValue(value.charAt(i)) * weights[i];
        }

        int remainder = sum % 11;

        return remainder < 2 ? 0 : 11 - remainder;
    }
}