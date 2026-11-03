package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PhoneTest {

    @ParameterizedTest
    @CsvSource({
            "(47) 3333-4444, 4733334444",
            "(47) 99999-8888, 47999998888",
            "47999998888, 47999998888"
    })
    void shouldAcceptPhoneWithTenOrElevenDigits(String input, String expected) {
        assertThat(new Phone(input).value()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "479999988881", "abc"})
    void shouldRejectPhoneWithInvalidLength(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Phone(input))
                .withMessage("Telefone inválido.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldRejectBlankPhone(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Phone(input))
                .withMessage("Telefone não pode ser vazio.");
    }
}
