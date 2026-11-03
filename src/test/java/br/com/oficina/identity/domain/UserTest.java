package br.com.oficina.identity.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Email;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserTest {

    private static final Email EMAIL = new Email("admin@oficina.com");

    @Test
    void shouldCreateActiveUser() {
        User user = User.create(" Admin ", EMAIL, "hash", Role.ADMIN);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getName()).isEqualTo("Admin");
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPasswordHash()).isEqualTo("hash");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void shouldRejectMissingRequiredData() {
        UUID id = UUID.randomUUID();

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new User(null, "Admin", EMAIL, "hash", Role.ADMIN, true));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new User(id, " ", EMAIL, "hash", Role.ADMIN, true));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new User(id, null, EMAIL, "hash", Role.ADMIN, true));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new User(id, "Admin", null, "hash", Role.ADMIN, true));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new User(id, "Admin", EMAIL, " ", Role.ADMIN, true));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new User(id, "Admin", EMAIL, null, Role.ADMIN, true));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new User(id, "Admin", EMAIL, "hash", null, true));
    }
}
