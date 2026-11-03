package br.com.oficina.inventory.application.dto;

import java.util.UUID;

public record StockDeduction(UUID itemId, int quantity) {
}
