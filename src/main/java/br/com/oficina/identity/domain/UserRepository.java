package br.com.oficina.identity.domain;

import br.com.oficina.shared.domain.valueobject.Email;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);
}
