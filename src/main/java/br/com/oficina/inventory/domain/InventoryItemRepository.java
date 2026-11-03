package br.com.oficina.inventory.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryItemRepository {

    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findById(UUID id);

    List<InventoryItem> findAllById(Collection<UUID> ids);

    boolean existsByCode(String code);

    List<InventoryItem> findAll();

    List<InventoryItem> findLowStock();

    void delete(UUID id);
}
