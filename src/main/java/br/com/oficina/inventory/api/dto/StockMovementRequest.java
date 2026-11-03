package br.com.oficina.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StockMovementRequest(
        @Schema(example = "10")
        @NotNull(message = "Quantidade é obrigatória.")
        @Positive(message = "Quantidade deve ser maior que zero.")
        Integer quantity,

        @Schema(example = "NF 12345 - Fornecedor Auto Peças")
        @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres.")
        String note
) {
}
