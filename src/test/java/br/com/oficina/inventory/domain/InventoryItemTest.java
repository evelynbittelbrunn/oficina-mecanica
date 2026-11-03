package br.com.oficina.inventory.domain;

import br.com.oficina.shared.domain.exception.ValidationException;
import br.com.oficina.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class InventoryItemTest {

    private static final Money PRICE = Money.of("35.90");

    @Test
    void shouldCreateItemWithZeroBalanceAndNormalizedCode() {
        InventoryItem item = InventoryItem.create(" flt-oleo-001 ", " Filtro de óleo ", InventoryItemType.PART, PRICE, 5);

        assertThat(item.getId()).isNotNull();
        assertThat(item.getCode()).isEqualTo("FLT-OLEO-001");
        assertThat(item.getName()).isEqualTo("Filtro de óleo");
        assertThat(item.getType()).isEqualTo(InventoryItemType.PART);
        assertThat(item.getUnitPrice()).isEqualTo(PRICE);
        assertThat(item.getStockQuantity()).isZero();
        assertThat(item.getMinimumStock()).isEqualTo(5);
        assertThat(item.getVersion()).isNull();
    }

    @Test
    void inboundShouldIncreaseBalanceAndRecordMovement() {
        InventoryItem item = item(10);

        StockMovement movement = item.move(StockMovementType.INBOUND, 5, "NF 123");

        assertThat(item.getStockQuantity()).isEqualTo(15);
        assertThat(movement.itemId()).isEqualTo(item.getId());
        assertThat(movement.type()).isEqualTo(StockMovementType.INBOUND);
        assertThat(movement.quantity()).isEqualTo(5);
        assertThat(movement.balanceAfter()).isEqualTo(15);
        assertThat(movement.reference()).isEqualTo("NF 123");
        assertThat(movement.occurredAt()).isNotNull();
    }

    @Test
    void outboundShouldDecreaseBalanceDownToZero() {
        InventoryItem item = item(10);

        assertThat(item.move(StockMovementType.OUTBOUND, 10, "OS-2026-000001").balanceAfter()).isZero();
        assertThat(item.getStockQuantity()).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = StockMovementType.class, names = {"OUTBOUND", "RESERVATION"})
    void decreasingMovementShouldFailWithoutBalanceAndKeepStock(StockMovementType type) {
        InventoryItem item = item(3);

        assertThatExceptionOfType(InsufficientStockException.class)
                .isThrownBy(() -> item.move(type, 4, null))
                .withMessageContaining("disponível 3, solicitado 4");

        assertThat(item.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void reversalShouldReturnItemsToStock() {
        InventoryItem item = item(2);

        item.move(StockMovementType.REVERSAL, 3, "Return");

        assertThat(item.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void shouldRejectInvalidMovement() {
        InventoryItem item = item(10);

        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> item.move(StockMovementType.INBOUND, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> item.move(null, 1, null));
    }

    @Test
    void shouldFlagLowStockWhenBalanceAtOrBelowMinimum() {
        assertThat(item(5).isLowStock()).isTrue();
        assertThat(item(4).isLowStock()).isTrue();
        assertThat(item(6).isLowStock()).isFalse();
    }

    @Test
    void shouldCheckAvailability() {
        InventoryItem item = item(3);

        assertThat(item.hasStock(3)).isTrue();
        assertThat(item.hasStock(4)).isFalse();
    }

    @Test
    void shouldUpdateRegistrationData() {
        InventoryItem item = item(3);

        item.update("Fluido", InventoryItemType.SUPPLY, Money.of("10"), 1);

        assertThat(item.getName()).isEqualTo("Fluido");
        assertThat(item.getType()).isEqualTo(InventoryItemType.SUPPLY);
        assertThat(item.getMinimumStock()).isEqualTo(1);
        assertThat(item.getStockQuantity()).isEqualTo(3);
    }

    @Test
    void shouldRejectInvalidData() {
        UUID id = UUID.randomUUID();
        InventoryItemType type = InventoryItemType.PART;

        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(null, "C", "N", type, PRICE, 0, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, " ", "N", type, PRICE, 0, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, null, "N", type, PRICE, 0, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, "C".repeat(41), "N", type, PRICE, 0, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, "C", "N", type, PRICE, -1, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, "C", "N", type, PRICE, null, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, "C", " ", type, PRICE, 0, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, "C", null, type, PRICE, 0, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, "C", "N".repeat(151), type, PRICE, 0, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, "C", "N", null, PRICE, 0, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, "C", "N", type, null, 0, 0, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, "C", "N", type, PRICE, 0, -1, null));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> new InventoryItem(id, "C", "N", type, PRICE, 0, null, null));
    }

    @Test
    void movementShouldValidateItsData() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new StockMovement(null, id, StockMovementType.INBOUND, 1, 1, null, now));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new StockMovement(id, null, StockMovementType.INBOUND, 1, 1, null, now));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new StockMovement(id, id, null, 1, 1, null, now));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new StockMovement(id, id, StockMovementType.INBOUND, 1, 1, null, null));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new StockMovement(id, id, StockMovementType.INBOUND, 0, 1, null, now));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new StockMovement(id, id, StockMovementType.INBOUND, 1, -1, null, now));
    }

    private InventoryItem item(int quantity) {
        return new InventoryItem(UUID.randomUUID(), "FLT-001", "Filtro", InventoryItemType.PART, PRICE, quantity, 5, 0L);
    }
}
