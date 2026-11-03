package br.com.oficina.servicing.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QuoteApproved(
        UUID serviceOrderId,
        String number,
        BigDecimal totalAmount,
        Instant occurredAt
) implements ServiceOrderEvent {
}
