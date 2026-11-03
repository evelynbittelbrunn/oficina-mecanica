package br.com.oficina.servicing.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record CompleteDiagnosisRequest(
        @Schema(example = "Óleo vencido e filtro saturado. Recomendada troca imediata.")
        @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres.")
        String notes
) {
}
