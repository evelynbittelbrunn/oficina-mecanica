# Architecture Decision Records (ADR)

Each file records one decision: context, decision and consequences. Short format based on
[Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

| # | Decision | Status |
|---|----------|--------|
| [0001](0001-spring-boot-3.md) | Spring Boot 3.5 (not 4.x) | Accepted |
| [0002](0002-modular-monolith-ddd.md) | Modular monolith with bounded contexts, code in English | Accepted |
| [0003](0003-jwt-hmac-resource-server.md) | HMAC JWT with spring-security-oauth2-resource-server | Accepted |
| [0004](0004-persistence-independent-domain.md) | JPA-free domain: repositories as ports, adapters in infrastructure | Accepted |
| [0005](0005-postgresql-flyway.md) | PostgreSQL 16 + Flyway, immutable migrations | Accepted |
| [0006](0006-catalog-and-inventory.md) | Soft delete of services and stock movements | Accepted |
| [0007](0007-service-order.md) | Service order: numbering, transactions, events and customer tracking | Accepted |
