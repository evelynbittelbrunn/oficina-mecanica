package br.com.oficina.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

interface InventoryItemJpaRepository extends JpaRepository<InventoryItemJpaEntity, UUID> {

    boolean existsByCode(String code);

    List<InventoryItemJpaEntity> findAllByOrderByName();

    @Query("select i from InventoryItemJpaEntity i where i.stockQuantity <= i.minimumStock order by i.name")
    List<InventoryItemJpaEntity> findLowStock();
}
