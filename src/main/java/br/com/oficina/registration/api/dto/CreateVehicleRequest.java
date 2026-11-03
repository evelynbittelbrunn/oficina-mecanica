package br.com.oficina.registration.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateVehicleRequest(
        @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "ID do cliente é obrigatório.")
        UUID customerId,

        @Schema(description = "Old format (ABC1234) or Mercosur (ABC1D23)", example = "ABC1D23")
        @NotBlank(message = "Placa é obrigatória.")
        String licensePlate,

        @Schema(example = "Volkswagen")
        @NotBlank(message = "Marca é obrigatória.")
        @Size(max = 100, message = "Marca deve ter no máximo 100 caracteres.")
        String brand,

        @Schema(example = "Gol")
        @NotBlank(message = "Modelo é obrigatório.")
        @Size(max = 100, message = "Modelo deve ter no máximo 100 caracteres.")
        String model,

        @Schema(description = "Between 1900 and next year", example = "2020")
        @NotNull(message = "Ano é obrigatório.")
        @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900.")
        Integer year
) {
}
