package br.com.oficina.registration.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateVehicleRequest(
        @Schema(example = "Volkswagen")
        @NotBlank(message = "Marca é obrigatória.")
        @Size(max = 100, message = "Marca deve ter no máximo 100 caracteres.")
        String brand,

        @Schema(example = "Gol G7")
        @NotBlank(message = "Modelo é obrigatório.")
        @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres.")
        String model,

        @Schema(example = "2021")
        @NotNull(message = "Ano é obrigatório.")
        @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900.")
        Integer year
) {
}
