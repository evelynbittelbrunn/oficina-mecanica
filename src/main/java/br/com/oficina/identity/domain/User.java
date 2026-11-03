package br.com.oficina.identity.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Email;

import java.util.UUID;

public class User {

    private final UUID id;
    private final String name;
    private final Email email;
    private final String passwordHash;
    private final Role role;
    private final boolean active;

    public User(UUID id, String name, Email email, String passwordHash, Role role, boolean active) {
        if (id == null) {
            throw new ValidationException("ID do usuário não pode ser nulo.");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nome do usuário não pode ser vazio.");
        }
        if (email == null) {
            throw new ValidationException("E-mail do usuário não pode ser nulo.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new ValidationException("Senha do usuário não pode ser vazia.");
        }
        if (role == null) {
            throw new ValidationException("Perfil do usuário não pode ser nulo.");
        }

        this.id = id;
        this.name = name.trim();
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public static User create(String name, Email email, String passwordHash, Role role) {
        return new User(UUID.randomUUID(), name, email, passwordHash, role, true);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
