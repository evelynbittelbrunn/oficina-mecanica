package br.com.oficina.servicing.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;

import java.util.UUID;

/**
 * Service order line (service or part). Keeps the name and unit price <b>at the time it was added</b>,
 * so later changes to the catalog or inventory do not alter quotes already issued.
 */
public abstract sealed class ServiceOrderItem permits ServiceItem, PartItem {

    private final UUID id;
    private final UUID referenceId;
    private final String name;
    private final Money unitPrice;
    private int quantity;

    protected ServiceOrderItem(UUID id, UUID referenceId, String name, Money unitPrice, int quantity) {
        if (id == null || referenceId == null) {
            throw new ValidationException("Item da OS deve ter identificador e referência.");
        }
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nome do item da OS não pode ser vazio.");
        }
        if (unitPrice == null) {
            throw new ValidationException("Preço do item da OS é obrigatório.");
        }
        validateQuantity(quantity);

        this.id = id;
        this.referenceId = referenceId;
        this.name = name.trim();
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantidade deve ser maior que zero.");
        }
    }

    void increase(int additionalQuantity) {
        validateQuantity(additionalQuantity);
        this.quantity += additionalQuantity;
    }

    boolean refersTo(UUID referenceId) {
        return this.referenceId.equals(referenceId);
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }

    public UUID getId() {
        return id;
    }

    protected UUID getReferenceId() {
        return referenceId;
    }

    public String getName() {
        return name;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
