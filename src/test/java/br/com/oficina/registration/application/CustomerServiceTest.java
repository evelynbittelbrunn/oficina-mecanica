package br.com.oficina.registration.application;

import br.com.oficina.registration.domain.Customer;
import br.com.oficina.registration.domain.CustomerRepository;
import br.com.oficina.registration.domain.VehicleRepository;
import br.com.oficina.shared.domain.exception.ConflictException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Document;
import br.com.oficina.shared.domain.valueobject.Email;
import br.com.oficina.shared.domain.valueobject.Phone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final Document DOCUMENT = new Document("52998224725");

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private CustomerService service;

    @Test
    void shouldCreateCustomerWithNormalizedData() {
        when(customerRepository.existsByDocument(DOCUMENT)).thenReturn(false);
        when(customerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer customer = service.createCustomer("Maria", "529.982.247-25", "MARIA@EMAIL.COM", "(11) 98888-7777");

        assertThat(customer.getDocument()).isEqualTo(DOCUMENT);
        assertThat(customer.getEmail().value()).isEqualTo("maria@email.com");
        assertThat(customer.getPhone().value()).isEqualTo("11988887777");
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldRejectDuplicatedDocument() {
        when(customerRepository.existsByDocument(DOCUMENT)).thenReturn(true);

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> service.createCustomer("Maria", "529.982.247-25", "maria@email.com", "11988887777"));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidDocumentWithoutQueryingRepository() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.createCustomer("Maria", "111.111.111-11", "maria@email.com", "11988887777"));

        verify(customerRepository, never()).existsByDocument(any());
    }

    @Test
    void shouldUpdateCustomer() {
        Customer existing = customer();
        when(customerRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);

        Customer updated = service.updateCustomer(existing.getId(), "Maria Souza", "nova@email.com", "1133334444");

        assertThat(updated.getName()).isEqualTo("Maria Souza");
        assertThat(updated.getEmail().value()).isEqualTo("nova@email.com");
    }

    @Test
    void shouldFindByIdOrFail() {
        Customer existing = customer();
        UUID missing = UUID.randomUUID();
        when(customerRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(customerRepository.findById(missing)).thenReturn(Optional.empty());

        assertThat(service.findCustomerById(existing.getId())).isSameAs(existing);
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> service.findCustomerById(missing));
    }

    @Test
    void shouldFindByDocumentOrFail() {
        Customer existing = customer();
        when(customerRepository.findByDocument(DOCUMENT))
                .thenReturn(Optional.of(existing))
                .thenReturn(Optional.empty());

        assertThat(service.findCustomerByDocument("529.982.247-25")).isSameAs(existing);
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> service.findCustomerByDocument("52998224725"));
    }

    @Test
    void shouldListCustomers() {
        List<Customer> customers = List.of(customer());
        when(customerRepository.findAll()).thenReturn(customers);

        assertThat(service.listCustomers()).isEqualTo(customers);
    }

    @Test
    void shouldDeleteCustomerWithoutVehicles() {
        UUID id = UUID.randomUUID();
        when(customerRepository.existsById(id)).thenReturn(true);
        when(vehicleRepository.existsByCustomerId(id)).thenReturn(false);

        service.deleteCustomer(id);

        verify(customerRepository).delete(id);
    }

    @Test
    void shouldNotDeleteCustomerWithVehicles() {
        UUID id = UUID.randomUUID();
        when(customerRepository.existsById(id)).thenReturn(true);
        when(vehicleRepository.existsByCustomerId(id)).thenReturn(true);

        assertThatExceptionOfType(ConflictException.class).isThrownBy(() -> service.deleteCustomer(id));

        verify(customerRepository, never()).delete(any());
    }

    @Test
    void shouldNotDeleteMissingCustomer() {
        UUID id = UUID.randomUUID();
        when(customerRepository.existsById(id)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> service.deleteCustomer(id));
    }

    private Customer customer() {
        return Customer.create("Maria", DOCUMENT, new Email("maria@email.com"), new Phone("11988887777"));
    }
}
