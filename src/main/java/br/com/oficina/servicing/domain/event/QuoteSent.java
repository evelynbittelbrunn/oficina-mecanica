package br.com.oficina.servicing.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QuoteSent(
        UUID serviceOrderId,
        String number,
        UUID customerId,
        BigDecimal totalAmount,
        Instant occurredAt
) implements ServiceOrderEvent {
}
