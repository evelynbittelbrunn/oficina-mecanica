package br.com.oficina.catalog.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServiceOfferingRequest(
        @Schema(example = "Troca de óleo")
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres.")
        String name,

        @Schema(example = "Troca do óleo do motor e do filtro de óleo")
        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres.")
        String description,

        @Schema(example = "120.00")
        @NotNull(message = "Preço base é obrigatório.")
        @DecimalMin(value = "0.00", message = "Preço base não pode ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "Preço base deve ter no máximo 2 casas decimais.")
        BigDecimal basePrice,

        @Schema(example = "45")
        @NotNull(message = "Tempo estimado é obrigatório.")
        @Positive(message = "Tempo estimado deve ser maior que zero.")
        Integer estimatedMinutes,

        @Schema(description = "Update only: reactivates (true) or deactivates (false) the service", example = "true")
        Boolean active
) {
}
