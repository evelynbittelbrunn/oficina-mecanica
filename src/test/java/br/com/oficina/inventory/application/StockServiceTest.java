package br.com.oficina.inventory.application;

import br.com.oficina.inventory.application.dto.StockDeduction;
import br.com.oficina.inventory.domain.InsufficientStockException;
import br.com.oficina.inventory.domain.InventoryItem;
import br.com.oficina.inventory.domain.InventoryItemRepository;
import br.com.oficina.inventory.domain.InventoryItemType;
import br.com.oficina.inventory.domain.StockMovement;
import br.com.oficina.inventory.domain.StockMovementRepository;
import br.com.oficina.inventory.domain.StockMovementType;
import br.com.oficina.shared.domain.exception.NotFoundException;
import br.com.oficina.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private InventoryItemRepository itemRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @InjectMocks
    private StockService service;

    @Test
    void shouldDeductAllItemsSummingRepeatedLines() {
        InventoryItem filter = item("Filtro", 10);
        InventoryItem oil = item("Óleo", 8);
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of(filter, oil));

        service.deduct(List.of(
                new StockDeduction(filter.getId(), 1),
                new StockDeduction(oil.getId(), 4),
                new StockDeduction(filter.getId(), 2)
        ), "OS-2026-000001");

        assertThat(filter.getStockQuantity()).isEqualTo(7);
        assertThat(oil.getStockQuantity()).isEqualTo(4);
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(movementRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .allMatch(movement -> movement.type() == StockMovementType.OUTBOUND
                        && "OS-2026-000001".equals(movement.reference()));
        verify(itemRepository).save(filter);
        verify(itemRepository).save(oil);
    }

    @Test
    void shouldChangeNothingIfAnyItemLacksBalance() {
        InventoryItem filter = item("Filtro", 10);
        InventoryItem pads = item("Pastilha", 1);
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of(filter, pads));

        assertThatExceptionOfType(InsufficientStockException.class)
                .isThrownBy(() -> service.deduct(List.of(
                        new StockDeduction(filter.getId(), 1),
                        new StockDeduction(pads.getId(), 2)
                ), "OS-2026-000001"))
                .withMessageContaining("Pastilha")
                .withMessageContaining("disponível 1, necessário 2");

        assertThat(filter.getStockQuantity()).isEqualTo(10);
        verify(itemRepository, never()).save(any());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void shouldFailIfItemDoesNotExist() {
        when(itemRepository.findAllById(anyCollection())).thenReturn(List.of());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> service.deduct(List.of(new StockDeduction(UUID.randomUUID(), 1)), "OS"));
    }

    @Test
    void emptyListShouldNotQueryStock() {
        service.deduct(List.of(), "OS-2026-000001");

        verifyNoInteractions(itemRepository, movementRepository);
    }

    private InventoryItem item(String name, int quantity) {
        return new InventoryItem(UUID.randomUUID(), name.toUpperCase(), name, InventoryItemType.PART,
                Money.of("10.00"), quantity, 1, 0L);
    }
}
