package br.com.oficina.identity.application;

import br.com.oficina.identity.domain.Role;
import br.com.oficina.identity.domain.User;
import br.com.oficina.identity.domain.UserRepository;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Email;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean createIfAbsent(String name, String email, String password, Role role) {
        Email userEmail = new Email(email);

        if (userRepository.existsByEmail(userEmail)) {
            return false;
        }

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Senha deve ter ao menos " + MIN_PASSWORD_LENGTH + " caracteres.");
        }

        userRepository.save(User.create(name, userEmail, passwordEncoder.encode(password), role));
        return true;
    }
}
