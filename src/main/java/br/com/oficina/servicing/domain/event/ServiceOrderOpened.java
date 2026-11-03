package br.com.oficina.servicing.domain.event;

import java.time.Instant;
import java.util.UUID;

public record ServiceOrderOpened(
        UUID serviceOrderId,
        String number,
        UUID customerId,
        UUID vehicleId,
        Instant occurredAt
) implements ServiceOrderEvent {
}
