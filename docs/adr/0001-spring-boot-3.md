# ADR 0001 — Spring Boot 3.5 (not 4.x)

**Status:** accepted

## Context
The project was generated with Spring Initializr using Spring Boot 4.1.2, but the Tech Challenge
requires **Spring Boot 3.x**. Besides the formal requirement, the ecosystem (springdoc-openapi 2.x,
Testcontainers and Spring Security examples) is consolidated on the 3.x line.

## Decision
Use **Spring Boot 3.5.16** (latest 3.x release available) with Java 21. Starters and test packages
were adjusted to the 3.x line (`spring-boot-starter-web`, `flyway-core`,
`org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest` etc.).
springdoc-openapi **2.10.0** is the version built against Boot 3.5.16.

## Consequences
- Meets the mandatory stack requirement.
- The 3.5 line is at the end of OSS support; the risk of unpatched CVEs is tracked by
  Dependency-Check and Trivy (see `docs/security/vulnerability-analysis.md`). Transitive libraries
  with fixes in compatible lines are upgraded through Boot version properties.
- A future migration to Boot 4 requires reviewing test packages and renamed starters.
