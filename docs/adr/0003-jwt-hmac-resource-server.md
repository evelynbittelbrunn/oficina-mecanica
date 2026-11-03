# ADR 0003 — HMAC JWT with spring-security-oauth2-resource-server

**Status:** accepted

## Context
Administrative endpoints require stateless authentication with `ADMIN` and `ATTENDANT` roles. The
statement accepts either `jjwt` or Spring's resource server with an HMAC key.

## Decision
- Use `spring-boot-starter-oauth2-resource-server` (Nimbus) with **HS256**: `NimbusJwtEncoder`
  issues and `NimbusJwtDecoder` validates signature, expiration and issuer. No extra dependency.
- Secret only through the `JWT_SECRET` environment variable (at least 32 bytes, validated on
  startup). Configurable expiration (`JWT_EXPIRATION`, ISO-8601, default `PT2H`).
- `roles` claim → `ROLE_*` authorities. Passwords with BCrypt.
- URL authorization:
  - public: `POST /api/v1/auth/login`, `/api/v1/public/**`, Swagger and `/actuator/health`;
  - `ADMIN`: writes to `/services` and `/inventory-items` (including stock-in/stock-out) and `/reports`;
  - `ADMIN` or `ATTENDANT`: every other `/api/v1/**` endpoint, including reading services and items;
  - any other route: denied.
- Customer endpoints (`/api/v1/public`) do not use JWT: they authenticate with the service order
  number + customer document pair. If the pair does not match, the response is 404 (does not reveal
  that the order exists).
- Demo users are created on startup by a seeder with passwords from the environment; only the `dev`
  profile has default values.

## Consequences
- Tokens cannot be revoked before they expire (acceptable for the MVP; short expiration).
- 401/403 follow the same RFC 7807 format as every other error.
