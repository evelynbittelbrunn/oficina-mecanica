# ADR 0002 — Modular monolith with bounded contexts, code in English

**Status:** accepted

## Context
The domain (auto repair shop) has subdomains with their own rules and vocabulary: customer/vehicle
registration, service catalog, parts inventory and servicing (service orders and quotes). The
original codebase already used English packages (`registration`, `catalog`, `inventory`,
`servicing`, `identity`, `shared`) and English identifiers, with user-facing messages in Portuguese.

## Decision
- **Modular monolith**: a single deployable, with one package per bounded context under
  `br.com.oficina`: `registration`, `catalog`, `inventory`, `servicing`, `identity` and `shared`
  (shared kernel).
- Each context has the layers `domain` → `application` → `infrastructure` / `api`.
  `domain` does not depend on Spring or JPA.
- Contexts talk to each other **only through application services** (synchronous calls, within the
  same transaction when the rule requires atomicity) or **domain events** published with
  `ApplicationEventPublisher`. No context accesses another context's repository.
- **All code is in English**: packages, classes, methods, fields, enums, tests, endpoints, JSON
  fields, tables and columns. The Portuguese terms of the business (and of the Tech Challenge
  statement) are mapped to English names in `docs/ddd/ubiquitous-language.md`.
- **Messages returned to API users are in Portuguese** (the shop and its customers are Brazilian),
  as in the original code.

## Consequences
- Explicit boundaries make it easier to extract a context as a service in the future.
- The glossary keeps the mapping business term (PT) → code (EN) explicit.
- Synchronous calls between contexts create runtime coupling, acceptable in a monolith.
