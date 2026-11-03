package br.com.oficina.inventory.api.dto;

import br.com.oficina.inventory.domain.InventoryItem;
import br.com.oficina.inventory.domain.InventoryItemType;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryItemResponse(
        UUID id,
        String code,
        String name,
        InventoryItemType type,
        BigDecimal unitPrice,
        int stockQuantity,
        int minimumStock,
        boolean lowStock
) {

    public static InventoryItemResponse from(InventoryItem item) {
        return new InventoryItemResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getType(),
                item.getUnitPrice().amount(),
                item.getStockQuantity(),
                item.getMinimumStock(),
                item.isLowStock()
        );
    }
}
