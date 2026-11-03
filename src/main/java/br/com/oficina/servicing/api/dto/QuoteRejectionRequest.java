package br.com.oficina.servicing.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuoteRejectionRequest(
        @Schema(description = "CPF/CNPJ of the customer who owns the service order", example = "529.982.247-25")
        @NotBlank(message = "Documento é obrigatório.")
        String document,

        @Schema(example = "Valor acima do orçamento disponível.")
        @NotBlank(message = "Motivo da rejeição é obrigatório.")
        @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres.")
        String reason
) {
}
