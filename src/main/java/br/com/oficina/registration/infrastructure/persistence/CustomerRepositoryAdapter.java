package br.com.oficina.registration.infrastructure.persistence;

import br.com.oficina.registration.domain.Customer;
import br.com.oficina.registration.domain.CustomerRepository;
import br.com.oficina.shared.domain.valueobject.Document;
import br.com.oficina.shared.domain.valueobject.Email;
import br.com.oficina.shared.domain.valueobject.Phone;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpa;

    CustomerRepositoryAdapter(CustomerJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Customer save(Customer customer) {
        jpa.save(new CustomerJpaEntity(
                customer.getId(),
                customer.getName(),
                customer.getDocument().value(),
                customer.getEmail().value(),
                customer.getPhone().value()
        ));
        return customer;
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpa.findById(id).map(CustomerRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Customer> findByDocument(Document document) {
        return jpa.findByDocument(document.value()).map(CustomerRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpa.existsById(id);
    }

    @Override
    public boolean existsByDocument(Document document) {
        return jpa.existsByDocument(document.value());
    }

    @Override
    public List<Customer> findAll() {
        return jpa.findAll(Sort.by("name")).stream().map(CustomerRepositoryAdapter::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    private static Customer toDomain(CustomerJpaEntity entity) {
        return new Customer(
                entity.getId(),
                entity.getName(),
                new Document(entity.getDocument()),
                new Email(entity.getEmail()),
                new Phone(entity.getPhone())
        );
    }
}
