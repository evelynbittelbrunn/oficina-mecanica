package br.com.oficina.inventory.api.dto;

import br.com.oficina.inventory.domain.InventoryItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateInventoryItemRequest(
        @Schema(example = "Filtro de óleo premium")
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
        String name,

        @Schema(example = "PART")
        @NotNull(message = "Tipo é obrigatório (PART ou SUPPLY).")
        InventoryItemType type,

        @Schema(example = "39.90")
        @NotNull(message = "Preço unitário é obrigatório.")
        @DecimalMin(value = "0.00", message = "Preço unitário não pode ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "Preço unitário deve ter no máximo 2 casas decimais.")
        BigDecimal unitPrice,

        @Schema(example = "5")
        @NotNull(message = "Estoque mínimo é obrigatório.")
        @PositiveOrZero(message = "Estoque mínimo não pode ser negativo.")
        Integer minimumStock
) {
}
