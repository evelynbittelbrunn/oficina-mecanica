package br.com.oficina.registration.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Document;
import br.com.oficina.shared.domain.valueobject.Email;
import br.com.oficina.shared.domain.valueobject.Phone;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CustomerTest {

    private static final Document DOCUMENT = new Document("529.982.247-25");
    private static final Email EMAIL = new Email("maria@email.com");
    private static final Phone PHONE = new Phone("(11) 98888-7777");

    @Test
    void shouldCreateCustomerWithGeneratedId() {
        Customer customer = Customer.create("  Maria  ", DOCUMENT, EMAIL, PHONE);

        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getName()).isEqualTo("Maria");
        assertThat(customer.getDocument()).isEqualTo(DOCUMENT);
        assertThat(customer.getEmail()).isEqualTo(EMAIL);
        assertThat(customer.getPhone()).isEqualTo(PHONE);
    }

    @Test
    void shouldGenerateDifferentIds() {
        assertThat(Customer.create("Maria", DOCUMENT, EMAIL, PHONE).getId())
                .isNotEqualTo(Customer.create("Maria", DOCUMENT, EMAIL, PHONE).getId());
    }

    @Test
    void shouldUpdateDataKeepingDocument() {
        Customer customer = Customer.create("Maria", DOCUMENT, EMAIL, PHONE);

        customer.update("Maria Souza", new Email("nova@email.com"), new Phone("1133334444"));

        assertThat(customer.getName()).isEqualTo("Maria Souza");
        assertThat(customer.getEmail().value()).isEqualTo("nova@email.com");
        assertThat(customer.getPhone().value()).isEqualTo("1133334444");
        assertThat(customer.getDocument()).isEqualTo(DOCUMENT);
    }

    @Test
    void shouldRejectNullId() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Customer(null, "Maria", DOCUMENT, EMAIL, PHONE));
    }

    @Test
    void shouldRejectNullDocument() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Customer.create("Maria", null, EMAIL, PHONE));
    }

    @Test
    void shouldRejectBlankOrNullName() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Customer.create(" ", DOCUMENT, EMAIL, PHONE));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Customer.create(null, DOCUMENT, EMAIL, PHONE));
    }

    @Test
    void shouldRejectTooLongName() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Customer.create("a".repeat(151), DOCUMENT, EMAIL, PHONE));
    }

    @Test
    void shouldRejectNullEmail() {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> Customer.create("Maria", DOCUMENT, null, PHONE));
    }

    @Test
    void shouldRejectNullPhone() {
        Customer customer = Customer.create("Maria", DOCUMENT, EMAIL, PHONE);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> customer.update("Maria", EMAIL, null));
    }

    @Test
    void shouldNotChangeCustomerWhenUpdateIsInvalid() {
        Customer customer = new Customer(UUID.randomUUID(), "Maria", DOCUMENT, EMAIL, PHONE);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> customer.update("", EMAIL, PHONE));

        assertThat(customer.getName()).isEqualTo("Maria");
    }
}
