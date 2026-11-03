package br.com.oficina.identity.infrastructure.persistence;

import br.com.oficina.identity.domain.User;
import br.com.oficina.identity.domain.UserRepository;
import br.com.oficina.shared.domain.valueobject.Email;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    UserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        jpa.save(new UserJpaEntity(
                user.getId(),
                user.getName(),
                user.getEmail().value(),
                user.getPasswordHash(),
                user.getRole(),
                user.isActive()
        ));
        return user;
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpa.findByEmail(email.value()).map(entity -> new User(
                entity.getId(),
                entity.getName(),
                new Email(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.isActive()
        ));
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpa.existsByEmail(email.value());
    }
}
