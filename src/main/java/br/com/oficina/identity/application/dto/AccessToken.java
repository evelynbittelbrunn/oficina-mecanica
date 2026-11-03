package br.com.oficina.identity.application.dto;

import java.time.Instant;

public record AccessToken(String token, String type, Instant expiresAt) {
}
