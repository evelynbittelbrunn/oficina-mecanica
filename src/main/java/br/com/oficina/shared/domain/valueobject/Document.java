package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;

public record Document(String value) {

    public enum Type { CPF, CNPJ }

    public Document {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Documento não pode ser vazio.");
        }

        value = Digits.extract(value);

        boolean valid = switch (value.length()) {
            case 11 -> Cpf.isValid(value);
            case 14 -> Cnpj.isValid(value);
            default -> false;
        };

        if (!valid) {
            throw new ValidationException("CPF ou CNPJ inválido.");
        }
    }

    public Type type() {
        return value.length() == 11 ? Type.CPF : Type.CNPJ;
    }

    public String formatted() {
        return type() == Type.CPF ? new Cpf(value).formatted() : new Cnpj(value).formatted();
    }
}
