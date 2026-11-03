package br.com.oficina.identity.application;

import br.com.oficina.identity.application.dto.AccessToken;
import br.com.oficina.identity.domain.User;
import br.com.oficina.identity.domain.UserRepository;
import br.com.oficina.shared.domain.exception.UnauthenticatedException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Email;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS = "E-mail ou senha inválidos.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenGenerator tokenGenerator
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    @Transactional(readOnly = true)
    public AccessToken authenticate(String email, String password) {
        User user = findUser(email)
                .filter(User::isActive)
                .filter(found -> password != null && passwordEncoder.matches(password, found.getPasswordHash()))
                .orElseThrow(() -> new UnauthenticatedException(INVALID_CREDENTIALS));

        return tokenGenerator.generate(user);
    }

    private Optional<User> findUser(String email) {
        try {
            return userRepository.findByEmail(new Email(email));
        } catch (ValidationException e) {
            // malformed e-mail: same response as invalid credentials, without revealing why
            return Optional.empty();
        }
    }
}
