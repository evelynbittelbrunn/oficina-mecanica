# ADR 0004 — JPA-free domain: repositories as ports, adapters in infrastructure

**Status:** accepted

## Context
The statement requires `domain` without Spring/JPA dependencies. The original code used JPA entities
and Spring Data repositories directly in the services.

## Decision
- Domain entities and aggregates are POJOs; repository interfaces (`CustomerRepository`,
  `ServiceOrderRepository`...) live in `domain`.
- `infrastructure` has separate JPA entities (`*JpaEntity`), package-private Spring Data repositories
  and adapters that convert domain ⇄ JPA.
- Optimistic locking: `@Version` on the service order and inventory item JPA entities; the domain
  carries the `version` field so that `merge` detects concurrent writes (→ HTTP 409).
- Pagination uses the framework-free `shared.domain.PageResult` instead of Spring Data's `Page`.

## Consequences
- Business rules are testable without a database or Spring (fast unit tests).
- More mapping code, concentrated in the adapters.
