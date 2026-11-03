# ADR 0006 — Soft delete of services and stock movements

**Status:** accepted

## Context
Old service orders reference services and inventory items. Stock must be traceable and can never go
negative, including under concurrency.

## Decision
- **Service** (`ServiceOffering`): `DELETE /services/{id}` is a **soft delete** (`active = false`);
  the service disappears from new orders but history stays intact. Reactivation via `PUT` with
  `active: true`. Only active services can be added to an order (violation → 422). The class is
  named `ServiceOffering` to avoid clashing with Spring's `@Service` annotation.
- **Inventory item** (`InventoryItem`, type `PART` or `SUPPLY`): the balance only changes through
  movements (`INBOUND`, `OUTBOUND`, `RESERVATION`, `REVERSAL`), each one recording the resulting
  balance. Deletion is physical and fails with 409 if the item is in any service order (FK).
  Movements of a deleted item are removed in cascade.
- Current use of the movement types: creating an item with an initial quantity and
  `POST /inventory-items/{id}/stock-in` produce `INBOUND`; `POST /inventory-items/{id}/stock-out`
  and quote approval produce `OUTBOUND` (with the order number as reference). `RESERVATION` and
  `REVERSAL` are already supported by the domain (decrease/increase the balance) and are available
  for the evolution "reserve parts when added to the order and reverse on rejection".
- **Deduction for an order** (`StockService.deduct`): validates every item before changing any
  balance and lists all missing items in the message (422). Runs inside the quote approval transaction.
- `@Version` on `inventory_items` prevents lost updates on concurrent deductions (409).
- "Low stock" = balance **less than or equal to** the minimum.

## Consequences
- Order and stock history are preserved; one more field (`active`) to filter on.
- Reserving stock when a part is added (to avoid approving quotes without parts) is left as an evolution.
