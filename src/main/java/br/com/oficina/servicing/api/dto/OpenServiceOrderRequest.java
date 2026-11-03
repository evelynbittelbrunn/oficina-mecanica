package br.com.oficina.servicing.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OpenServiceOrderRequest(
        @Schema(description = "CPF or CNPJ of an already registered customer", example = "529.982.247-25")
        @NotBlank(message = "Documento do cliente é obrigatório.")
        String customerDocument,

        @Schema(description = "Vehicle by license plate; brand, model and year are only required if the plate is not registered")
        @NotNull(message = "Veículo é obrigatório.")
        @Valid
        VehicleRequest vehicle,

        @Schema(description = "Initial services (optional)")
        List<@Valid ItemRequest> services,

        @Schema(description = "Initial parts/supplies (optional)")
        List<@Valid ItemRequest> parts
) {

    public record VehicleRequest(
            @Schema(example = "ABC1D23")
            @NotBlank(message = "Placa é obrigatória.")
            String licensePlate,
            @Schema(example = "Volkswagen")
            String brand,
            @Schema(example = "Gol")
            String model,
            @Schema(example = "2020")
            Integer year
    ) {
    }
}
