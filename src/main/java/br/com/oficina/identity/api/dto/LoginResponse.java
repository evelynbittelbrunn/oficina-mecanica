package br.com.oficina.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record LoginResponse(
        @Schema(description = "JWT for the Authorization header", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token,
        @Schema(example = "Bearer")
        String type,
        @Schema(description = "Expiration instant (UTC)")
        Instant expiresAt
) {
}
