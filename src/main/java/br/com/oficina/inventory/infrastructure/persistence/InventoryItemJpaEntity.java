package br.com.oficina.inventory.infrastructure.persistence;

import br.com.oficina.inventory.domain.InventoryItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
public class InventoryItemJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InventoryItemType type;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock;

    @Version
    private Long version;

    protected InventoryItemJpaEntity() {
    }

    public InventoryItemJpaEntity(
            UUID id,
            String code,
            String name,
            InventoryItemType type,
            BigDecimal unitPrice,
            Integer stockQuantity,
            Integer minimumStock,
            Long version
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.minimumStock = minimumStock;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public InventoryItemType getType() {
        return type;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public Long getVersion() {
        return version;
    }
}
