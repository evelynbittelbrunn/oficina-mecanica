package br.com.oficina.servicing.domain;

import java.time.Instant;
import java.util.UUID;

public record ServiceOrderFilter(ServiceOrderStatus status, UUID customerId, Instant openedFrom, Instant openedTo) {

    public static ServiceOrderFilter empty() {
        return new ServiceOrderFilter(null, null, null, null);
    }
}
