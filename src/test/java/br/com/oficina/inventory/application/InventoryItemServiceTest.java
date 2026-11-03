package br.com.oficina.inventory.application;

import br.com.oficina.inventory.domain.InsufficientStockException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryItemServiceTest {

    @Mock
    private InventoryItemRepository itemRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @InjectMocks
    private InventoryItemService service;

    @Test
    void shouldCreateItemWithInitialStockRecordingInbound() {
        when(itemRepository.existsByCode("FLT-001")).thenReturn(false);

        InventoryItem item = service.createItem("flt-001", "Filtro", InventoryItemType.PART, new BigDecimal("35.90"), 20, 5);

        assertThat(item.getStockQuantity()).isEqualTo(20);
        verify(itemRepository).save(item);
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository).save(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(StockMovementType.INBOUND);
        assertThat(captor.getValue().quantity()).isEqualTo(20);
    }

    @Test
    void shouldCreateItemWithoutInitialStockAndWithoutMovement() {
        when(itemRepository.existsByCode("FLT-001")).thenReturn(false);

        assertThat(service.createItem("FLT-001", "Filtro", InventoryItemType.PART, BigDecimal.TEN, null, 5).getStockQuantity()).isZero();
        assertThat(service.createItem("FLT-001", "Filtro", InventoryItemType.PART, BigDecimal.TEN, 0, 5).getStockQuantity()).isZero();

        verify(movementRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicatedCodeOrNegativeQuantity() {
        when(itemRepository.existsByCode("FLT-001")).thenReturn(true).thenReturn(false);

        assertThatExceptionOfType(ConflictException.class)
                .isThrownBy(() -> service.createItem("FLT-001", "Filtro", InventoryItemType.PART, BigDecimal.TEN, 1, 5));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.createItem("FLT-001", "Filtro", InventoryItemType.PART, BigDecimal.TEN, -1, 5));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.createItem(" ", "Filtro", InventoryItemType.PART, BigDecimal.TEN, 1, 5));
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> service.createItem(null, "Filtro", InventoryItemType.PART, null, 1, 5));
    }

    @Test
    void shouldUpdateItem() {
        InventoryItem item = item(10);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        service.updateItem(item.getId(), "Filtro premium", InventoryItemType.PART, new BigDecimal("40"), 2);

        assertThat(item.getName()).isEqualTo("Filtro premium");
        assertThat(item.getUnitPrice()).isEqualTo(Money.of("40"));
    }

    @Test
    void shouldRegisterStockInAndOut() {
        InventoryItem item = item(10);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThat(service.registerStockIn(item.getId(), 5, "NF 1").getStockQuantity()).isEqualTo(15);
        assertThat(service.registerStockOut(item.getId(), 3, "Loss").getStockQuantity()).isEqualTo(12);

        verify(movementRepository, times(2)).save(any());
    }

    @Test
    void stockOutWithoutBalanceShouldNotPersist() {
        InventoryItem item = item(1);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThatExceptionOfType(InsufficientStockException.class)
                .isThrownBy(() -> service.registerStockOut(item.getId(), 2, null));

        verify(itemRepository, never()).save(any());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void shouldListQueries() {
        InventoryItem item = item(1);
        List<StockMovement> movements = List.of(item.move(StockMovementType.INBOUND, 1, null));
        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(itemRepository.findLowStock()).thenReturn(List.of(item));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(movementRepository.findAllByItemId(item.getId())).thenReturn(movements);

        assertThat(service.listItems()).containsExactly(item);
        assertThat(service.listLowStockItems()).containsExactly(item);
        assertThat(service.listMovements(item.getId())).isEqualTo(movements);
    }

    @Test
    void shouldDeleteOrFailIfMissing() {
        InventoryItem item = item(1);
        UUID missing = UUID.randomUUID();
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.findById(missing)).thenReturn(Optional.empty());

        service.deleteItem(item.getId());

        verify(itemRepository).delete(item.getId());
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> service.deleteItem(missing));
    }

    private InventoryItem item(int quantity) {
        return new InventoryItem(UUID.randomUUID(), "FLT-001", "Filtro", InventoryItemType.PART,
                Money.of("35.90"), quantity, 5, 0L);
    }
}
