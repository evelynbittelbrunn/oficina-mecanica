package br.com.oficina.inventory.infrastructure.persistence;

import br.com.oficina.inventory.domain.StockMovement;
import br.com.oficina.inventory.domain.StockMovementRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
class StockMovementRepositoryAdapter implements StockMovementRepository {

    private final StockMovementJpaRepository jpa;

    StockMovementRepositoryAdapter(StockMovementJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(StockMovement movement) {
        jpa.save(new StockMovementJpaEntity(
                movement.id(),
                movement.itemId(),
                movement.type(),
                movement.quantity(),
                movement.balanceAfter(),
                movement.reference(),
                movement.occurredAt()
        ));
    }

    @Override
    public List<StockMovement> findAllByItemId(UUID itemId) {
        return jpa.findAllByItemIdOrderByOccurredAtDesc(itemId).stream()
                .map(entity -> new StockMovement(
                        entity.getId(),
                        entity.getItemId(),
                        entity.getType(),
                        entity.getQuantity(),
                        entity.getBalanceAfter(),
                        entity.getReference(),
                        entity.getOccurredAt()
                ))
                .toList();
    }
}
