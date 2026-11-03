package br.com.oficina.inventory.application;

import br.com.oficina.inventory.domain.InventoryItem;
import br.com.oficina.inventory.domain.InventoryItemRepository;
import br.com.oficina.inventory.domain.InventoryItemType;
import br.com.oficina.inventory.domain.StockMovement;
import br.com.oficina.inventory.domain.StockMovementRepository;
import br.com.oficina.inventory.domain.StockMovementType;
import br.com.oficina.shared.domain.exception.ConflictException;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryItemService {

    private static final String INITIAL_STOCK = "Estoque inicial";

    private final InventoryItemRepository itemRepository;
    private final StockMovementRepository movementRepository;

    public InventoryItemService(InventoryItemRepository itemRepository, StockMovementRepository movementRepository) {
        this.itemRepository = itemRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public InventoryItem createItem(
            String code,
            String name,
            InventoryItemType type,
            BigDecimal unitPrice,
            Integer initialQuantity,
            Integer minimumStock
    ) {
        if (code != null && !code.isBlank() && itemRepository.existsByCode(InventoryItem.normalizeCode(code))) {
            throw new ConflictException("Já existe uma peça/insumo com este código.");
        }
        if (initialQuantity != null && initialQuantity < 0) {
            throw new ValidationException("Quantidade inicial não pode ser negativa.");
        }

        InventoryItem item = InventoryItem.create(code, name, type, money(unitPrice), minimumStock);
        StockMovement movement = initialQuantity == null || initialQuantity == 0
                ? null
                : item.move(StockMovementType.INBOUND, initialQuantity, INITIAL_STOCK);

        itemRepository.save(item);
        if (movement != null) {
            movementRepository.save(movement);
        }
        return item;
    }

    @Transactional
    public InventoryItem updateItem(
            UUID id,
            String name,
            InventoryItemType type,
            BigDecimal unitPrice,
            Integer minimumStock
    ) {
        InventoryItem item = findItemById(id);
        item.update(name, type, money(unitPrice), minimumStock);
        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public InventoryItem findItemById(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Peça/insumo não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> listItems() {
        return itemRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<InventoryItem> listLowStockItems() {
        return itemRepository.findLowStock();
    }

    @Transactional(readOnly = true)
    public List<StockMovement> listMovements(UUID id) {
        findItemById(id);
        return movementRepository.findAllByItemId(id);
    }

    @Transactional
    public void deleteItem(UUID id) {
        findItemById(id);
        itemRepository.delete(id);
    }

    @Transactional
    public InventoryItem registerStockIn(UUID id, int quantity, String note) {
        return move(id, StockMovementType.INBOUND, quantity, note);
    }

    @Transactional
    public InventoryItem registerStockOut(UUID id, int quantity, String note) {
        return move(id, StockMovementType.OUTBOUND, quantity, note);
    }

    private InventoryItem move(UUID id, StockMovementType type, int quantity, String note) {
        InventoryItem item = findItemById(id);
        StockMovement movement = item.move(type, quantity, note);

        itemRepository.save(item);
        movementRepository.save(movement);
        return item;
    }

    private static Money money(BigDecimal amount) {
        return amount == null ? null : new Money(amount);
    }
}
