package br.com.oficina.registration.domain;

import br.com.oficina.shared.domain.valueobject.Document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByDocument(Document document);

    boolean existsById(UUID id);

    boolean existsByDocument(Document document);

    List<Customer> findAll();

    void delete(UUID id);
}
