package br.com.oficina.servicing.application.dto;

import br.com.oficina.registration.domain.Customer;
import br.com.oficina.registration.domain.Vehicle;
import br.com.oficina.servicing.domain.ServiceOrder;

public record ServiceOrderDetails(ServiceOrder order, Customer customer, Vehicle vehicle) {
}
