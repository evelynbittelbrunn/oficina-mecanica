package br.com.oficina.registration.api.dto;

import br.com.oficina.registration.domain.Customer;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String document,
        String documentType,
        String email,
        String phone
) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getDocument().value(),
                customer.getDocument().type().name(),
                customer.getEmail().value(),
                customer.getPhone().value()
        );
    }
}
