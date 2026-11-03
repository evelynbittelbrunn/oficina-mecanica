package br.com.oficina.inventory.application;

import br.com.oficina.inventory.application.dto.StockDeduction;
import br.com.oficina.inventory.domain.InsufficientStockException;
import br.com.oficina.inventory.domain.InventoryItem;
import br.com.oficina.inventory.domain.InventoryItemRepository;
import br.com.oficina.inventory.domain.StockMovementRepository;
import br.com.oficina.inventory.domain.StockMovementType;
import br.com.oficina.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final InventoryItemRepository itemRepository;
    private final StockMovementRepository movementRepository;

    public StockService(InventoryItemRepository itemRepository, StockMovementRepository movementRepository) {
        this.itemRepository = itemRepository;
        this.movementRepository = movementRepository;
    }

    /**
     * Deducts (OUTBOUND) all items at once, within the caller's transaction. If any item lacks
     * balance, nothing is changed and an {@link InsufficientStockException} listing every missing
     * item is thrown.
     */
    @Transactional
    public void deduct(List<StockDeduction> deductions, String reference) {
        Map<UUID, Integer> quantities = deductions.stream().collect(Collectors.toMap(
                StockDeduction::itemId, StockDeduction::quantity, Integer::sum, LinkedHashMap::new));

        if (quantities.isEmpty()) {
            return;
        }

        Map<UUID, InventoryItem> items = itemRepository.findAllById(quantities.keySet()).stream()
                .collect(Collectors.toMap(InventoryItem::getId, Function.identity()));

        List<String> missing = quantities.entrySet().stream()
                .filter(entry -> {
                    InventoryItem item = items.get(entry.getKey());
                    if (item == null) {
                        throw new NotFoundException("Peça/insumo " + entry.getKey() + " não encontrado.");
                    }
                    return !item.hasStock(entry.getValue());
                })
                .map(entry -> describeShortage(items.get(entry.getKey()), entry.getValue()))
                .toList();

        if (!missing.isEmpty()) {
            throw new InsufficientStockException("Estoque insuficiente: " + String.join("; ", missing) + ".");
        }

        quantities.forEach((itemId, quantity) -> {
            InventoryItem item = items.get(itemId);
            movementRepository.save(item.move(StockMovementType.OUTBOUND, quantity, reference));
            itemRepository.save(item);
        });
    }

    private static String describeShortage(InventoryItem item, int quantity) {
        return item.getName() + " (" + item.getCode() + ") disponível " + item.getStockQuantity()
                + ", necessário " + quantity;
    }
}
