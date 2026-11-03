package br.com.oficina.servicing.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record QuoteDecisionRequest(
        @Schema(description = "CPF/CNPJ of the customer who owns the service order", example = "529.982.247-25")
        @NotBlank(message = "Documento é obrigatório.")
        String document
) {
}
