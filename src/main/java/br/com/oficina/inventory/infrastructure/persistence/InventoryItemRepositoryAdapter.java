package br.com.oficina.inventory.infrastructure.persistence;

import br.com.oficina.inventory.domain.InventoryItem;
import br.com.oficina.inventory.domain.InventoryItemRepository;
import br.com.oficina.shared.domain.valueobject.Money;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class InventoryItemRepositoryAdapter implements InventoryItemRepository {

    private final InventoryItemJpaRepository jpa;

    InventoryItemRepositoryAdapter(InventoryItemJpaRepository jpa) {
        this.jpa = jpa;
    }

    /** {@code merge} with the loaded version: if another transaction saved first, throws OptimisticLockingFailureException. */
    @Override
    public InventoryItem save(InventoryItem item) {
        jpa.save(new InventoryItemJpaEntity(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getType(),
                item.getUnitPrice().amount(),
                item.getStockQuantity(),
                item.getMinimumStock(),
                item.getVersion()
        ));
        return item;
    }

    @Override
    public Optional<InventoryItem> findById(UUID id) {
        return jpa.findById(id).map(InventoryItemRepositoryAdapter::toDomain);
    }

    @Override
    public List<InventoryItem> findAllById(Collection<UUID> ids) {
        return jpa.findAllById(ids).stream().map(InventoryItemRepositoryAdapter::toDomain).toList();
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCode(code);
    }

    @Override
    public List<InventoryItem> findAll() {
        return jpa.findAllByOrderByName().stream().map(InventoryItemRepositoryAdapter::toDomain).toList();
    }

    @Override
    public List<InventoryItem> findLowStock() {
        return jpa.findLowStock().stream().map(InventoryItemRepositoryAdapter::toDomain).toList();
    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
        jpa.flush();
    }

    private static InventoryItem toDomain(InventoryItemJpaEntity entity) {
        return new InventoryItem(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getType(),
                new Money(entity.getUnitPrice()),
                entity.getStockQuantity(),
                entity.getMinimumStock(),
                entity.getVersion()
        );
    }
}
