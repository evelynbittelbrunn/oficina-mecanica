package br.com.oficina.servicing.domain.event;

import java.time.Instant;
import java.util.UUID;

public record VehicleDelivered(
        UUID serviceOrderId,
        String number,
        UUID vehicleId,
        Instant occurredAt
) implements ServiceOrderEvent {
}
