package br.com.oficina.registration.application;

import br.com.oficina.registration.domain.Customer;
import br.com.oficina.registration.domain.CustomerRepository;
import br.com.oficina.registration.domain.VehicleRepository;
import br.com.oficina.shared.domain.exception.ConflictException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.valueobject.Document;
import br.com.oficina.shared.domain.valueobject.Email;
import br.com.oficina.shared.domain.valueobject.Phone;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    public CustomerService(CustomerRepository customerRepository, VehicleRepository vehicleRepository) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Customer createCustomer(String name, String document, String email, String phone) {
        Document customerDocument = new Document(document);

        if (customerRepository.existsByDocument(customerDocument)) {
            throw new ConflictException("Já existe um cliente cadastrado com este documento.");
        }

        Customer customer = Customer.create(name, customerDocument, new Email(email), new Phone(phone));

        return customerRepository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(UUID id, String name, String email, String phone) {
        Customer customer = findCustomerById(id);
        customer.update(name, new Email(email), new Phone(phone));
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer findCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado."));
    }

    @Transactional(readOnly = true)
    public Customer findCustomerByDocument(String document) {
        return customerRepository.findByDocument(new Document(document))
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado para o documento informado."));
    }

    @Transactional(readOnly = true)
    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    @Transactional
    public void deleteCustomer(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new NotFoundException("Cliente não encontrado.");
        }
        if (vehicleRepository.existsByCustomerId(id)) {
            throw new ConflictException("Cliente possui veículos cadastrados e não pode ser removido.");
        }

        customerRepository.delete(id);
    }
}
