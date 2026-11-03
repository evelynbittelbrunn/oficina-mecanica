package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CnpjTest {

    @ParameterizedTest
    @ValueSource(strings = {"11.222.333/0001-81", "11222333000181"})
    void shouldAcceptValidCnpjWithOrWithoutMask(String input) {
        assertThat(new Cnpj(input).value()).isEqualTo("11222333000181");
    }

    @Test
    void shouldAcceptCnpjWhoseCheckDigitIsZero() {
        assertThat(new Cnpj("11.222.338/0001-04").value()).isEqualTo("11222338000104");
    }

    @Test
    void shouldFormatCnpj() {
        assertThat(new Cnpj("11222333000181").formatted()).isEqualTo("11.222.333/0001-81");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "11.222.333/0001-91", // wrong first check digit
            "11.222.333/0001-82", // wrong second check digit
            "00.000.000/0000-00", // all digits equal
            "1122233300018",      // invalid length
            "529.982.247-25"      // a CPF is not a CNPJ
    })
    void shouldRejectInvalidCnpj(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Cnpj(input))
                .withMessage("CNPJ inválido.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldRejectBlankCnpj(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Cnpj(input))
                .withMessage("CNPJ não pode ser vazio.");
    }
}
