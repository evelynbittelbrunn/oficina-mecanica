package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CpfTest {

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "52998224725", " 529.982.247-25 "})
    void shouldAcceptValidCpfWithOrWithoutMask(String input) {
        assertThat(new Cpf(input).value()).isEqualTo("52998224725");
    }

    @Test
    void shouldAcceptCpfWhoseCheckDigitIsZeroFromRemainderTen() {
        assertThat(new Cpf("100.000.001-08").value()).isEqualTo("10000000108");
    }

    @Test
    void shouldFormatCpf() {
        assertThat(new Cpf("52998224725").formatted()).isEqualTo("529.982.247-25");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "529.982.247-35",    // wrong first check digit
            "529.982.247-26",    // wrong second check digit
            "111.111.111-11",    // all digits equal
            "123456789",         // invalid length
            "11.222.333/0001-81" // a CNPJ is not a CPF
    })
    void shouldRejectInvalidCpf(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Cpf(input))
                .withMessage("CPF inválido.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldRejectBlankCpf(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Cpf(input))
                .withMessage("CPF não pode ser vazio.");
    }
}
