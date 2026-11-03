package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EmailTest {

    @ParameterizedTest
    @CsvSource({
            "evelyn@email.com, evelyn@email.com",
            "'  evelyn@email.com  ', evelyn@email.com",
            "EVELYN@EMAIL.COM, evelyn@email.com",
            "nome.sobrenome+tag@empresa.com.br, nome.sobrenome+tag@empresa.com.br"
    })
    void shouldAcceptAndNormalizeEmail(String input, String expected) {
        assertThat(new Email(input).value()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"evelynemail.com", "evelyn@", "evelyn@email", "@email.com", "evelyn@email.c"})
    void shouldRejectInvalidEmail(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Email(input))
                .withMessage("E-mail inválido.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldRejectBlankEmail(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Email(input))
                .withMessage("E-mail não pode ser vazio.");
    }
}
