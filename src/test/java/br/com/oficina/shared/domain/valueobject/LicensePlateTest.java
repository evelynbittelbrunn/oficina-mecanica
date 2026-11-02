package br.com.oficina.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class LicensePlateTest {

    @Test
    void shouldCreateLicensePlateWithOldFormat() {
        LicensePlate plate = new LicensePlate("ABC-1234");

        assertThat(plate.value()).isEqualTo("ABC1234");
    }

    @Test
    void shouldCreateLicensePlateWithMercosurFormat() {
        LicensePlate plate = new LicensePlate("ABC1D23");

        assertThat(plate.value()).isEqualTo("ABC1D23");
    }

    @Test
    void shouldConvertLowercaseLettersToUppercase() {
        LicensePlate plate = new LicensePlate("abc-1234");

        assertThat(plate.value()).isEqualTo("ABC1234");
    }

    @Test
    void shouldRejectInvalidLicensePlate() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new LicensePlate("ABC-123"));
    }

    @Test
    void shouldRejectNullLicensePlate() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new LicensePlate(null));
    }

    @Test
    void shouldRejectEmptyLicensePlate() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new LicensePlate(""));
    }

    @Test
    void shouldRejectBlankLicensePlate() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new LicensePlate("   "));
    }
}
