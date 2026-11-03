package br.com.oficina.servicing.domain.event;

import java.time.Instant;
import java.util.UUID;

public record ExecutionFinished(
        UUID serviceOrderId,
        String number,
        Instant occurredAt
) implements ServiceOrderEvent {
}
