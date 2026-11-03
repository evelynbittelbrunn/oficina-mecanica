# ADR 0007 — Service order: numbering, transactions, events and customer tracking

**Status:** accepted

## Context
The service order is the central aggregate: it needs a readable number for the customer, atomicity
between approval and inventory, traceability (history and events) and a channel for the customer to
track and decide the quote without an account in the system.

## Decision
- **Readable number** `OS-<year>-<6-digit sequence>` (e.g. `OS-2026-000123`; "OS" is the abbreviation
  Brazilian customers know for *ordem de serviço*). The sequence comes from `service_order_number_seq`
  (global, does not restart per year — avoids collisions and concurrency issues); the year is the
  opening year.
- **State machine inside the aggregate**: each operation requires a source status
  (`InvalidTransitionException` → 409) and `ServiceOrderStatus.next()` documents and guards the
  transitions. Completing the diagnosis without a service is a business rule (422).
- **Items** keep the name and price at the time they were added. Adding the same service/part again
  increases the quantity keeping the original price; removal is by service/item id.
- **Approval + inventory in the same transaction**: `CustomerTrackingService.approveQuote` performs
  the transition on the aggregate and calls `StockService.deduct`; if stock is missing, the exception
  (422) rolls everything back. `@Version` on the order and the item guards against concurrent writes (409).
- **Domain events**: the aggregate collects events; `ServiceOrderWriter` saves and publishes them with
  `ApplicationEventPublisher`. `@TransactionalEventListener` listeners run only **after commit**
  (audit log and simulated quote notification to the customer).
- **History actor**: the JWT `sub` (employee e-mail) on administrative routes; `customer` on public
  routes. The public view does not expose the actor nor internal ids.
- **Customer tracking** (`/api/v1/public/**`): authentication by order number + CPF/CNPJ. If the pair
  does not match (or the order does not exist) the response is **404**, so the existence of other
  customers' orders is not revealed.
- **Average execution time report**: computed in SQL from the history (entering `IN_PROGRESS` until
  the `IN_PROGRESS → FINISHED` transition); rejected orders are excluded. The period filters by the
  finishing date, with inclusive dates in the `America/Sao_Paulo` time zone. Per service, it considers
  the orders that contain the service.

## Consequences
- Simple numbering, safe under concurrency; gaps in the sequence may happen (rollbacks).
- A real notification (e-mail/SMS) can replace the listener without touching the domain.
- Authenticating the customer by number + document is adequate for the MVP; an access token sent
  along with the quote would be the natural evolution.
