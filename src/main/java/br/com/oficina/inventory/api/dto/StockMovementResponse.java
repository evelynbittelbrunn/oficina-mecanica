package br.com.oficina.inventory.api.dto;

import br.com.oficina.inventory.domain.StockMovement;
import br.com.oficina.inventory.domain.StockMovementType;

import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        StockMovementType type,
        int quantity,
        int balanceAfter,
        String reference,
        Instant occurredAt
) {

    public static StockMovementResponse from(StockMovement movement) {
        return new StockMovementResponse(
                movement.id(),
                movement.type(),
                movement.quantity(),
                movement.balanceAfter(),
                movement.reference(),
                movement.occurredAt()
        );
    }
}
