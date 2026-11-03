package br.com.oficina.servicing.api.dto;

import br.com.oficina.servicing.application.dto.ServiceOrderDetails;
import br.com.oficina.servicing.domain.ServiceOrder;
import br.com.oficina.servicing.domain.ServiceOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TrackingResponse(
        String number,
        ServiceOrderStatus status,
        Instant openedAt,
        String licensePlate,
        String vehicle,
        List<ItemResponse> services,
        List<ItemResponse> parts,
        BigDecimal totalAmount,
        String diagnosisNotes,
        QuoteResponse quote,
        List<StatusHistoryResponse> history
) {

    public static TrackingResponse from(ServiceOrderDetails details) {
        ServiceOrder order = details.order();
        return new TrackingResponse(
                order.getNumber().value(),
                order.getStatus(),
                order.getOpenedAt(),
                details.vehicle().getLicensePlate().value(),
                details.vehicle().getBrand() + " " + details.vehicle().getModel() + " " + details.vehicle().getYear(),
                order.getServiceItems().stream().map(ItemResponse::from).toList(),
                order.getPartItems().stream().map(ItemResponse::from).toList(),
                order.calculateTotal().amount(),
                order.getDiagnosisNotes(),
                QuoteResponse.from(order.getQuote()),
                order.getHistory().stream().map(StatusHistoryResponse::forCustomer).toList());
    }
}
