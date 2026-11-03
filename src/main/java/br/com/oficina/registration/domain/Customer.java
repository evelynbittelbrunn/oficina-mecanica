package br.com.oficina.registration.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Document;
import br.com.oficina.shared.domain.valueobject.Email;
import br.com.oficina.shared.domain.valueobject.Phone;

import java.util.UUID;

public class Customer {

    private static final int MAX_NAME_LENGTH = 150;

    private final UUID id;
    private final Document document;
    private String name;
    private Email email;
    private Phone phone;

    public Customer(UUID id, String name, Document document, Email email, Phone phone) {
        if (id == null) {
            throw new ValidationException("ID do cliente não pode ser nulo.");
        }
        if (document == null) {
            throw new ValidationException("Documento não pode ser nulo.");
        }

        this.id = id;
        this.document = document;
        apply(name, email, phone);
    }

    public static Customer create(String name, Document document, Email email, Phone phone) {
        return new Customer(UUID.randomUUID(), name, document, email, phone);
    }

    public void update(String name, Email email, Phone phone) {
        apply(name, email, phone);
    }

    private void apply(String name, Email email, Phone phone) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nome do cliente não pode ser vazio.");
        }
        if (name.trim().length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Nome do cliente deve ter no máximo " + MAX_NAME_LENGTH + " caracteres.");
        }
        if (email == null) {
            throw new ValidationException("E-mail não pode ser nulo.");
        }
        if (phone == null) {
            throw new ValidationException("Telefone não pode ser nulo.");
        }

        this.name = name.trim();
        this.email = email;
        this.phone = phone;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Document getDocument() {
        return document;
    }

    public Email getEmail() {
        return email;
    }

    public Phone getPhone() {
        return phone;
    }
}
