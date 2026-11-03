package br.com.oficina.servicing.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ItemRequest(
        @Schema(description = "Service (catalog) or inventory item ID", example = "11111111-0000-0000-0000-000000000001")
        @NotNull(message = "ID do item é obrigatório.")
        UUID id,

        @Schema(description = "Quantity (defaults to 1)", example = "1")
        @Positive(message = "Quantidade deve ser maior que zero.")
        Integer quantity
) {
}
