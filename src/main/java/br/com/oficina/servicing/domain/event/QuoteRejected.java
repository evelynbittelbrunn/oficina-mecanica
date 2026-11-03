package br.com.oficina.servicing.domain.event;

import java.time.Instant;
import java.util.UUID;

public record QuoteRejected(
        UUID serviceOrderId,
        String number,
        String reason,
        Instant occurredAt
) implements ServiceOrderEvent {
}
