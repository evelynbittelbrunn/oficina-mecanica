# ADR 0005 — PostgreSQL 16 + Flyway, immutable migrations

**Status:** accepted

## Context
The system controls inventory and quotes, which need atomicity (approving a quote and deducting
parts in the same transaction) and uniqueness (license plate, document, item code, order number).

## Decision
- **PostgreSQL 16**: ACID transactions, referential integrity, `UNIQUE`/`CHECK` constraints,
  sequences for the readable order number, maturity and native Testcontainers support.
- **Flyway** versions the schema; Hibernate only validates it (`ddl-auto=validate`).
- Migrations keep the original `V1__create_customers_and_vehicles.sql` and add one migration per
  context (`V2` users, `V3` services, `V4` inventory, `V5` service orders). Once applied, a
  migration is never edited; changes go into a new version.
- Sample data lives in `db/seed` (repeatable migration), loaded only in the `dev` and `docker` profiles.

## Consequences
- The schema evolves in a traceable, reproducible way in every environment.
- Integration tests use the same database as production (no dialect differences).
