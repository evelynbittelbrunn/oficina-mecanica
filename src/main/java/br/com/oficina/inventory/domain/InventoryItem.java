package br.com.oficina.inventory.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;

import java.util.Locale;
import java.util.UUID;

public class InventoryItem {

    private static final int MAX_CODE_LENGTH = 40;
    private static final int MAX_NAME_LENGTH = 150;

    private final UUID id;
    private final String code;
    private String name;
    private InventoryItemType type;
    private Money unitPrice;
    private int stockQuantity;
    private int minimumStock;
    private final Long version;

    public InventoryItem(
            UUID id,
            String code,
            String name,
            InventoryItemType type,
            Money unitPrice,
            Integer stockQuantity,
            Integer minimumStock,
            Long version
    ) {
        if (id == null) {
            throw new ValidationException("ID da peça não pode ser nulo.");
        }
        if (code == null || code.isBlank()) {
            throw new ValidationException("Código da peça não pode ser vazio.");
        }
        if (code.trim().length() > MAX_CODE_LENGTH) {
            throw new ValidationException("Código deve ter no máximo " + MAX_CODE_LENGTH + " caracteres.");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new ValidationException("Quantidade em estoque não pode ser negativa.");
        }

        this.id = id;
        this.code = normalizeCode(code);
        this.stockQuantity = stockQuantity;
        this.version = version;
        apply(name, type, unitPrice, minimumStock);
    }

    public static InventoryItem create(
            String code,
            String name,
            InventoryItemType type,
            Money unitPrice,
            Integer minimumStock
    ) {
        return new InventoryItem(UUID.randomUUID(), code, name, type, unitPrice, 0, minimumStock, null);
    }

    public static String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    public void update(String name, InventoryItemType type, Money unitPrice, Integer minimumStock) {
        apply(name, type, unitPrice, minimumStock);
    }

    public StockMovement move(StockMovementType type, int quantity, String reference) {
        if (type == null) {
            throw new ValidationException("Tipo de movimentação é obrigatório.");
        }
        if (quantity <= 0) {
            throw new ValidationException("Quantidade da movimentação deve ser maior que zero.");
        }
        if (type.sign() < 0 && !hasStock(quantity)) {
            throw new InsufficientStockException(
                    "Estoque insuficiente para " + name + " (" + code + "): disponível "
                            + stockQuantity + ", solicitado " + quantity + ".");
        }

        stockQuantity += type.sign() * quantity;

        return StockMovement.record(id, type, quantity, stockQuantity, reference);
    }

    public boolean hasStock(int quantity) {
        return stockQuantity >= quantity;
    }

    public boolean isLowStock() {
        return stockQuantity <= minimumStock;
    }

    private void apply(String name, InventoryItemType type, Money unitPrice, Integer minimumStock) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Nome da peça não pode ser vazio.");
        }
        if (name.trim().length() > MAX_NAME_LENGTH) {
            throw new ValidationException("Nome deve ter no máximo " + MAX_NAME_LENGTH + " caracteres.");
        }
        if (type == null) {
            throw new ValidationException("Tipo (PART ou SUPPLY) é obrigatório.");
        }
        if (unitPrice == null) {
            throw new ValidationException("Preço unitário é obrigatório.");
        }
        if (minimumStock == null || minimumStock < 0) {
            throw new ValidationException("Estoque mínimo não pode ser negativo.");
        }

        this.name = name.trim();
        this.type = type;
        this.unitPrice = unitPrice;
        this.minimumStock = minimumStock;
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

    public Money getUnitPrice() {
        return unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public Long getVersion() {
        return version;
    }
}
