package br.com.oficina.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "admin@oficina.com")
        @NotBlank(message = "E-mail é obrigatório.")
        String email,

        @Schema(example = "admin123")
        @NotBlank(message = "Senha é obrigatória.")
        String password
) {
}
