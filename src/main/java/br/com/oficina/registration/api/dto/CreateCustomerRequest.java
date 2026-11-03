package br.com.oficina.registration.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @Schema(example = "Maria da Silva")
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres.")
        String name,

        @Schema(description = "CPF or CNPJ, masked or not", example = "529.982.247-25")
        @NotBlank(message = "Documento é obrigatório.")
        String document,

        @Schema(example = "maria@email.com")
        @NotBlank(message = "E-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,

        @Schema(example = "(11) 98888-7777")
        @NotBlank(message = "Telefone é obrigatório.")
        String phone
) {
}
