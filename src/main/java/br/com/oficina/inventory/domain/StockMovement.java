package br.com.oficina.inventory.domain;

import br.com.oficina.shared.domain.exception.ValidationException;

import java.time.Instant;
import java.util.UUID;

public record StockMovement(
        UUID id,
        UUID itemId,
        StockMovementType type,
        int quantity,
        int balanceAfter,
        String reference,
        Instant occurredAt
) {

    public StockMovement {
        if (id == null || itemId == null || type == null || occurredAt == null) {
            throw new ValidationException("Movimentação de estoque incompleta.");
        }
        if (quantity <= 0) {
            throw new ValidationException("Quantidade da movimentação deve ser maior que zero.");
        }
        if (balanceAfter < 0) {
            throw new ValidationException("Saldo de estoque não pode ficar negativo.");
        }
    }

    static StockMovement record(UUID itemId, StockMovementType type, int quantity, int balanceAfter, String reference) {
        return new StockMovement(UUID.randomUUID(), itemId, type, quantity, balanceAfter, reference, Instant.now());
    }
}
