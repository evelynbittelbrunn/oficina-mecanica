package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LicensePlateTest {

    @ParameterizedTest
    @CsvSource({
            "ABC1234, ABC1234",
            "ABC-1234, ABC1234",
            "abc-1234, ABC1234",
            "ABC 1234, ABC1234"
    })
    void shouldAcceptOldFormat(String input, String expected) {
        LicensePlate plate = new LicensePlate(input);

        assertThat(plate.value()).isEqualTo(expected);
        assertThat(plate.mercosur()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "ABC1D23, ABC1D23",
            "abc1d23, ABC1D23",
            "ABC-1D23, ABC1D23"
    })
    void shouldAcceptMercosurFormat(String input, String expected) {
        LicensePlate plate = new LicensePlate(input);

        assertThat(plate.value()).isEqualTo(expected);
        assertThat(plate.mercosur()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC123", "AB12345", "ABCD123", "1234ABC", "ABC12D3", "ABC1DD3"})
    void shouldRejectInvalidPlate(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new LicensePlate(input))
                .withMessage("Placa inválida.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldRejectBlankPlate(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new LicensePlate(input))
                .withMessage("Placa não pode ser vazia.");
    }

    @Test
    void equalNormalizedPlatesShouldBeEqual() {
        assertThat(new LicensePlate("abc-1d23")).isEqualTo(new LicensePlate("ABC1D23"));
    }
}
