package br.com.oficina.servicing.domain;

import br.com.oficina.shared.domain.exception.InvalidTransitionException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;

import java.time.Instant;

/**
 * Service order quote: generated automatically when the diagnosis is completed (services + parts)
 * and decided only once by the customer. Immutable: each decision produces a new instance.
 */
public record Quote(
        Money totalAmount,
        QuoteStatus status,
        Instant sentAt,
        Instant decidedAt,
        String rejectionReason
) {

    public Quote {
        if (totalAmount == null || status == null || sentAt == null) {
            throw new ValidationException("Orçamento incompleto.");
        }
        if (status != QuoteStatus.PENDING && decidedAt == null) {
            throw new ValidationException("Orçamento decidido precisa da data da decisão.");
        }
    }

    static Quote generate(Money totalAmount, Instant sentAt) {
        return new Quote(totalAmount, QuoteStatus.PENDING, sentAt, null, null);
    }

    Quote approve(Instant decidedAt) {
        requirePending();
        return new Quote(totalAmount, QuoteStatus.APPROVED, sentAt, decidedAt, null);
    }

    Quote reject(String reason, Instant decidedAt) {
        requirePending();
        return new Quote(totalAmount, QuoteStatus.REJECTED, sentAt, decidedAt, reason);
    }

    private void requirePending() {
        if (status != QuoteStatus.PENDING) {
            throw new InvalidTransitionException("Orçamento já foi decidido (" + status + ").");
        }
    }
}
