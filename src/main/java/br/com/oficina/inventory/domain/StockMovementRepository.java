package br.com.oficina.inventory.domain;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository {

    void save(StockMovement movement);

    List<StockMovement> findAllByItemId(UUID itemId);
}
