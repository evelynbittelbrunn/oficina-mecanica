package br.com.oficina.servicing.api.dto;

import br.com.oficina.registration.domain.Customer;
import br.com.oficina.registration.domain.Vehicle;
import br.com.oficina.servicing.application.dto.ServiceOrderDetails;
import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ServiceOrderResponse(
        UUID id,
        String number,
        ServiceOrderStatus status,
        Instant openedAt,
        CustomerSummary customer,
        VehicleSummary vehicle,
        List<ItemResponse> services,
        List<ItemResponse> parts,
        BigDecimal totalAmount,
        String diagnosisNotes,
        QuoteResponse quote,
        List<StatusHistoryResponse> history
) {

    public record CustomerSummary(UUID id, String name, String document) {
    }

    public record VehicleSummary(UUID id, String licensePlate, String brand, String model, int year) {
    }

    public static ServiceOrderResponse from(ServiceOrderDetails details) {
        ServiceOrder order = details.order();
        Customer customer = details.customer();
        Vehicle vehicle = details.vehicle();
        return new ServiceOrderResponse(
                order.getId(),
                order.getNumber().value(),
                order.getStatus(),
                order.getOpenedAt(),
                new CustomerSummary(customer.getId(), customer.getName(), customer.getDocument().value()),
                new VehicleSummary(vehicle.getId(), vehicle.getLicensePlate().value(), vehicle.getBrand(),
                        vehicle.getModel(), vehicle.getYear()),
                order.getServiceItems().stream().map(ItemResponse::from).toList(),
                order.getPartItems().stream().map(ItemResponse::from).toList(),
                order.calculateTotal().amount(),
                order.getDiagnosisNotes(),
                QuoteResponse.from(order.getQuote()),
                order.getHistory().stream().map(StatusHistoryResponse::internal).toList());
    }
}
