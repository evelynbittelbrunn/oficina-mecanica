package br.com.oficina.shared.domain.valueobject;

import java.util.Locale;
import java.util.regex.Pattern;

public record LicensePlate(String value) {

    private static final Pattern MERCOSUR_PATTERN =
            Pattern.compile("[A-Z]{3}[0-9][A-Z][0-9]{2}");

    private static final Pattern OLD_PATTERN =
            Pattern.compile("[A-Z]{3}[0-9]{4}");

    public LicensePlate {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Placa não pode ser vazia.");
        }

        value = value.replace("-", "")
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);

        if (!MERCOSUR_PATTERN.matcher(value).matches()
                && !OLD_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Placa inválida.");
        }
    }
}

