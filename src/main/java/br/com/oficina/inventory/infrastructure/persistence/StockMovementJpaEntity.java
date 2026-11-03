package br.com.oficina.inventory.infrastructure.persistence;

import br.com.oficina.inventory.domain.StockMovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovementJpaEntity {

    @Id
    private UUID id;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StockMovementType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(length = 255)
    private String reference;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected StockMovementJpaEntity() {
    }

    public StockMovementJpaEntity(
            UUID id,
            UUID itemId,
            StockMovementType type,
            Integer quantity,
            Integer balanceAfter,
            String reference,
            Instant occurredAt
    ) {
        this.id = id;
        this.itemId = itemId;
        this.type = type;
        this.quantity = quantity;
        this.balanceAfter = balanceAfter;
        this.reference = reference;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getItemId() {
        return itemId;
    }

    public StockMovementType getType() {
        return type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public String getReference() {
        return reference;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
