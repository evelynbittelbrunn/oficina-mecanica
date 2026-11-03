package br.com.oficina.servicing.api.dto;

import br.com.oficina.servicing.domain.ServiceOrderStatus;
import br.com.oficina.servicing.domain.StatusHistoryEntry;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StatusHistoryResponse(
        ServiceOrderStatus previousStatus,
        ServiceOrderStatus newStatus,
        Instant changedAt,
        String actor,
        String note
) {

    public static StatusHistoryResponse internal(StatusHistoryEntry entry) {
        return new StatusHistoryResponse(entry.previousStatus(), entry.newStatus(), entry.changedAt(),
                entry.actor(), entry.note());
    }

    public static StatusHistoryResponse forCustomer(StatusHistoryEntry entry) {
        return new StatusHistoryResponse(entry.previousStatus(), entry.newStatus(), entry.changedAt(),
                null, entry.note());
    }
}
