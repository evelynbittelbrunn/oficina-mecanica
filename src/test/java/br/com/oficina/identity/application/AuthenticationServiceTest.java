package br.com.oficina.identity.application;

import br.com.oficina.identity.application.dto.AccessToken;
import br.com.oficina.identity.domain.Role;
import br.com.oficina.identity.domain.User;
import br.com.oficina.identity.domain.UserRepository;
import br.com.oficina.shared.domain.exception.UnauthenticatedException;
import br.com.oficina.shared.domain.valueobject.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final Email EMAIL = new Email("admin@oficina.com");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenGenerator tokenGenerator;

    @InjectMocks
    private AuthenticationService service;

    @Test
    void shouldIssueTokenForValidCredentials() {
        User user = user(true);
        AccessToken token = new AccessToken("jwt", "Bearer", Instant.now());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
        when(tokenGenerator.generate(user)).thenReturn(token);

        assertThat(service.authenticate("ADMIN@oficina.com", "password123")).isEqualTo(token);
    }

    @Test
    void shouldRejectWrongPassword() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(true)));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatExceptionOfType(UnauthenticatedException.class)
                .isThrownBy(() -> service.authenticate("admin@oficina.com", "wrong"));
        verify(tokenGenerator, never()).generate(any());
    }

    @Test
    void shouldRejectMissingOrInactiveUserOrNullPassword() {
        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(user(false)))
                .thenReturn(Optional.of(user(true)));

        assertThatExceptionOfType(UnauthenticatedException.class)
                .isThrownBy(() -> service.authenticate("admin@oficina.com", "password123"));
        assertThatExceptionOfType(UnauthenticatedException.class)
                .isThrownBy(() -> service.authenticate("admin@oficina.com", "password123"));
        assertThatExceptionOfType(UnauthenticatedException.class)
                .isThrownBy(() -> service.authenticate("admin@oficina.com", null));
    }

    @Test
    void shouldTreatMalformedEmailAsInvalidCredentials() {
        assertThatExceptionOfType(UnauthenticatedException.class)
                .isThrownBy(() -> service.authenticate("not-an-email", "password123"))
                .withMessage("E-mail ou senha inválidos.");
        verify(userRepository, never()).findByEmail(any());
    }

    private User user(boolean active) {
        return new User(UUID.randomUUID(), "Admin", EMAIL, "hash", Role.ADMIN, active);
    }
}
