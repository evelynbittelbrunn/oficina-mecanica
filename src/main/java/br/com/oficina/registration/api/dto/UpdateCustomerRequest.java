package br.com.oficina.registration.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @Schema(example = "Maria da Silva Souza")
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
        String name,

        @Schema(example = "maria.souza@email.com")
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,

        @Schema(example = "(11) 97777-6666")
        @NotBlank(message = "Telefone é obrigatório.")
        String phone
) {
}
