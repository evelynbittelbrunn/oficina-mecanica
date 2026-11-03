package br.com.oficina.servicing.application.dto;

import java.util.UUID;

public record RequestedItem(UUID id, Integer quantity) {
}
