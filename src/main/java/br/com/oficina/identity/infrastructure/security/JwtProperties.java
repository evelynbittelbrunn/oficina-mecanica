package br.com.oficina.identity.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration expiration, String issuer) {

    private static final int MIN_SECRET_LENGTH = 32;

    public JwtProperties {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "Set the JWT_SECRET environment variable with at least " + MIN_SECRET_LENGTH + " characters.");
        }
        if (expiration == null || expiration.isNegative() || expiration.isZero()) {
            throw new IllegalStateException("app.jwt.expiration must be a positive duration (e.g. PT2H).");
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "oficina-mecanica";
        }
    }
}
