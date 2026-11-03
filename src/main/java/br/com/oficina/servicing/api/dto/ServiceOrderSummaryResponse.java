package br.com.oficina.servicing.api.dto;

import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ServiceOrderSummaryResponse(
        UUID id,
        String number,
        ServiceOrderStatus status,
        UUID customerId,
        UUID vehicleId,
        Instant openedAt,
        BigDecimal totalAmount
) {

    public static ServiceOrderSummaryResponse from(ServiceOrder order) {
        return new ServiceOrderSummaryResponse(
                order.getId(),
                order.getNumber().value(),
                order.getStatus(),
                order.getCustomerId(),
                order.getVehicleId(),
                order.getOpenedAt(),
                order.calculateTotal().amount());
    }
}
