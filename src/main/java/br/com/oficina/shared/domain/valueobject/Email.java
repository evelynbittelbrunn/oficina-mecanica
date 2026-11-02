package br.com.oficina.shared.domain.valueobject;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
                    Pattern.CASE_INSENSITIVE);

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("E-mail não pode ser vazio.");
        }

        value = value.trim().toLowerCase(Locale.ROOT);

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
    }
}
