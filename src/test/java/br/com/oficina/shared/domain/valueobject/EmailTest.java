package br.com.oficina.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class EmailTest {

    @Test
    void shouldCreateEmailWithValidAddress() {
        Email email = new Email("evelyn@email.com");

        assertThat(email.value()).isEqualTo("evelyn@email.com");
    }

    @Test
    void shouldRemoveLeadingAndTrailingSpaces() {
        Email email = new Email("  evelyn@email.com  ");

        assertThat(email.value()).isEqualTo("evelyn@email.com");
    }

    @Test
    void shouldConvertEmailToLowercase() {
        Email email = new Email("EVELYN@EMAIL.COM");

        assertThat(email.value()).isEqualTo("evelyn@email.com");
    }

    @Test
    void shouldRejectEmailWithoutAtSymbol() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Email("evelynemail.com"));
    }

    @Test
    void shouldRejectEmailWithoutDomain() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Email("evelyn@"));
    }

    @Test
    void shouldRejectEmailWithoutExtension() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Email("evelyn@email"));
    }

    @Test
    void shouldRejectNullEmail() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Email(null));
    }

    @Test
    void shouldRejectEmptyEmail() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Email(""));
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Email("   "));
    }
}
