package br.com.oficina.identity.application;

import br.com.oficina.identity.domain.Role;
import br.com.oficina.identity.domain.User;
import br.com.oficina.identity.domain.UserRepository;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    private static final Email EMAIL = new Email("admin@oficina.com");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegistrationService service;

    @Test
    void shouldCreateUserWithEncodedPassword() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode("secure-password")).thenReturn("bcrypt-hash");

        assertThat(service.createIfAbsent("Admin", "admin@oficina.com", "secure-password", Role.ADMIN)).isTrue();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldNotRecreateExistingUser() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThat(service.createIfAbsent("Admin", "admin@oficina.com", "secure-password", Role.ADMIN)).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectShortOrNullPassword() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.createIfAbsent("Admin", "admin@oficina.com", "short", Role.ADMIN));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.createIfAbsent("Admin", "admin@oficina.com", null, Role.ADMIN));
    }
}
