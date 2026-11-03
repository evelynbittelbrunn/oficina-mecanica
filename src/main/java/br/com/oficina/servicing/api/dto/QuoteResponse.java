package br.com.oficina.servicing.api.dto;

import br.com.oficina.servicing.domain.Quote;
import br.com.oficina.servicing.domain.QuoteStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record QuoteResponse(
        BigDecimal totalAmount,
        QuoteStatus status,
        Instant sentAt,
        Instant decidedAt,
        String rejectionReason
) {

    public static QuoteResponse from(Quote quote) {
        return quote == null ? null : new QuoteResponse(
                quote.totalAmount().amount(),
                quote.status(),
                quote.sentAt(),
                quote.decidedAt(),
                quote.rejectionReason());
    }
}
