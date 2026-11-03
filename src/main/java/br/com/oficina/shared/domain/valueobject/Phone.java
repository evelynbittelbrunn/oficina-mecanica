package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;

import java.util.regex.Pattern;

public record Phone(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");

    public Phone {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Telefone não pode ser vazio.");
        }

        value = Digits.extract(value);

        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new ValidationException("Telefone inválido.");
        }
    }
}
