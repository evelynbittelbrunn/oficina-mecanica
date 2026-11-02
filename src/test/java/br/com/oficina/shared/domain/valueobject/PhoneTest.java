package br.com.oficina.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class PhoneTest {

    @Test
    void shouldCreatePhoneWithTenDigits() {
        Phone phone = new Phone("(47) 3333-4444");

        assertThat(phone.value()).isEqualTo("4733334444");
    }

    @Test
    void shouldCreatePhoneWithElevenDigits() {
        Phone phone = new Phone("(47) 99999-8888");

        assertThat(phone.value()).isEqualTo("47999998888");
    }

    @Test
    void shouldAcceptPhoneWithoutFormatting() {
        Phone phone = new Phone("47999998888");

        assertThat(phone.value()).isEqualTo("47999998888");
    }

    @Test
    void shouldRejectPhoneWithInvalidLength() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Phone("123456789"));
    }

    @Test
    void shouldRejectNullPhone() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Phone(null));
    }

    @Test
    void shouldRejectEmptyPhone() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Phone(""));
    }

    @Test
    void shouldRejectBlankPhone() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Phone("   "));
    }
}
