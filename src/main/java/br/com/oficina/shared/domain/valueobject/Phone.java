package br.com.oficina.shared.domain.valueobject;

import java.util.regex.Pattern;

public record Phone(String value) {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{10,11}$");

    public Phone {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Telefone não pode ser vazio.");
        }

        value = value.replaceAll("\\D", "");

        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Telefone inválido.");
        }
    }
}
