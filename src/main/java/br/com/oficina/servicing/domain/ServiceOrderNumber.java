package br.com.oficina.servicing.domain;

import br.com.oficina.shared.domain.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

/** Human-readable service order number given to the customer: {@code OS-<year>-<6-digit sequence>}, e.g. OS-2026-000123. */
public record ServiceOrderNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("^OS-\\d{4}-\\d{6,}$");

    public ServiceOrderNumber {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Número da OS não pode ser vazio.");
        }

        value = value.trim().toUpperCase(Locale.ROOT);

        if (!FORMAT.matcher(value).matches()) {
            throw new ValidationException("Número da OS inválido. Formato esperado: OS-AAAA-NNNNNN.");
        }
    }

    public static ServiceOrderNumber generate(int year, long sequence) {
        if (sequence <= 0) {
            throw new ValidationException("Sequencial da OS deve ser positivo.");
        }
        return new ServiceOrderNumber(String.format("OS-%04d-%06d", year, sequence));
    }

    @Override
    public String toString() {
        return value;
    }
}
