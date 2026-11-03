package br.com.oficina.servicing.domain;

import br.com.oficina.shared.domain.valueobject.Money;

import java.util.UUID;

public final class PartItem extends ServiceOrderItem {

    public PartItem(UUID id, UUID inventoryItemId, String name, Money unitPrice, int quantity) {
        super(id, inventoryItemId, name, unitPrice, quantity);
    }

    static PartItem create(UUID inventoryItemId, String name, Money unitPrice, int quantity) {
        return new PartItem(UUID.randomUUID(), inventoryItemId, name, unitPrice, quantity);
    }

    public UUID getInventoryItemId() {
        return getReferenceId();
    }
}
