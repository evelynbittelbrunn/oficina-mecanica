# Ubiquitous language

Glossary of the terms used by the shop, the code and the documentation. The business speaks
Portuguese; the code is written in English. This table is the official mapping between both, so
conversations with the business and the code refer to the same concepts. Root package: `br.com.oficina`.

## Servicing (`servicing`)

| Business term (PT) | English term | Meaning | Code |
|---|---|---|---|
| **Ordem de Serviço (OS)** | Service order | Record of a vehicle's service, from reception to delivery. Root of the servicing aggregate. | `servicing.domain.ServiceOrder` |
| **Número da OS** | Service order number | Readable identifier given to the customer, format `OS-YYYY-NNNNNN` (e.g. `OS-2026-000123`). | `servicing.domain.ServiceOrderNumber` |
| **Status da OS** | Service order status | Stage of the service: received, in diagnosis, awaiting approval, in progress, finished, delivered. | `servicing.domain.ServiceOrderStatus` |
| **Recebida** | Received | Vehicle checked in; not evaluated yet. | `ServiceOrderStatus.RECEIVED` |
| **Diagnóstico** | Diagnosis | Mechanic's evaluation to identify the needed services and parts. | `ServiceOrder.startDiagnosis` / `completeDiagnosis`; `IN_DIAGNOSIS` |
| **Observações do diagnóstico** | Diagnosis notes | Mechanic's written report, recorded when the diagnosis is completed. | `ServiceOrder.diagnosisNotes` |
| **Item de serviço** | Service item | Catalog service added to the order, with the price at the time it was added and the quantity. | `servicing.domain.ServiceItem` |
| **Item de peça** | Part item | Inventory part or supply added to the order, with the price at the time and the quantity. | `servicing.domain.PartItem` |
| **Orçamento** | Quote | Price proposal sent to the customer: sum of services and parts. Generated automatically when the diagnosis is completed. | `servicing.domain.Quote` |
| **Status do orçamento** | Quote status | Pending (waiting for the customer), approved or rejected. | `servicing.domain.QuoteStatus` |
| **Envio do orçamento** | Quote sent | Communication of the quote to the customer (simulated with a log entry in the MVP). | `QuoteSent` event; `CustomerNotificationListener` |
| **Aprovação** | Approval | The customer accepts the quote; execution starts and parts are deducted from stock. | `ServiceOrder.approveQuote`; `CustomerTrackingService.approveQuote` |
| **Rejeição** | Rejection | The customer declines the quote (with a reason); the order is finished without execution. | `ServiceOrder.rejectQuote` |
| **Execução** | Execution | Carrying out the approved services. | `IN_PROGRESS`; `ServiceOrder.finishExecution` |
| **Finalizada** | Finished | Services done (or quote rejected); vehicle ready for pickup. | `ServiceOrderStatus.FINISHED` |
| **Entrega** | Delivery | Vehicle returned to the customer; closes the service. | `ServiceOrder.deliver`; `DELIVERED` |
| **Histórico de status** | Status history | Timeline of the order: previous status, new status, timestamp, actor and note. | `servicing.domain.StatusHistoryEntry` |
| **Ator** | Actor | Who performed a transition: employee e-mail, `customer` or `system`. | `StatusHistoryEntry.actor` |
| **Acompanhamento** | Tracking | The customer checks the order using order number + document. | `CustomerTrackingService`, `PublicTrackingController` |
| **Tempo médio de execução** | Average execution time | Average time between start of execution and finishing, overall and per service. | `ReportService`, `ExecutionTimeQuery` |

## Registration (`registration`)

| Business term (PT) | English term | Meaning | Code |
|---|---|---|---|
| **Cliente** | Customer | Individual or company that owns the vehicle, identified by the document. | `registration.domain.Customer` |
| **Documento (CPF/CNPJ)** | Document | CPF (individual) or CNPJ (company), with valid check digits. | `shared.domain.valueobject.Document`, `Cpf`, `Cnpj` |
| **Veículo** | Vehicle | A customer's car, identified by the license plate. | `registration.domain.Vehicle` |
| **Placa** | License plate | Old format (`ABC1234`) or Mercosur (`ABC1D23`). | `shared.domain.valueobject.LicensePlate` |
| **Contato** | Contact | Customer e-mail and phone (with area code). | `shared.domain.valueobject.Email`, `Phone` |

## Catalog (`catalog`)

| Business term (PT) | English term | Meaning | Code |
|---|---|---|---|
| **Serviço** | Service (offering) | Work offered by the shop (e.g. oil change), with base price and estimated time. | `catalog.domain.ServiceOffering` |
| **Preço base** | Base price | List price of the service; copied to the order item when added. | `ServiceOffering.basePrice` |
| **Tempo estimado** | Estimated time | Expected duration of the service, in minutes. | `ServiceOffering.estimatedMinutes` |
| **Serviço inativo** | Inactive service | Service no longer offered; not allowed in new orders, history preserved. | `ServiceOffering.deactivate()` |

## Inventory (`inventory`)

| Business term (PT) | English term | Meaning | Code |
|---|---|---|---|
| **Peça** | Part | Component replaced in the vehicle (e.g. brake pads, filter). | `inventory.domain.InventoryItemType.PART` |
| **Insumo** | Supply | Material consumed by the service (e.g. oil, fluid, grease). | `InventoryItemType.SUPPLY` |
| **Peça/Insumo** | Inventory item | Item kept in stock, with unique code, unit price and balance. | `inventory.domain.InventoryItem` |
| **Saldo (quantidade em estoque)** | Stock quantity | Available quantity; never negative. | `InventoryItem.stockQuantity` |
| **Estoque mínimo** | Minimum stock | Balance from which the item needs restocking. | `InventoryItem.minimumStock`, `isLowStock()` |
| **Estoque baixo** | Low stock | Item with balance at or below the minimum. | `GET /inventory-items/low-stock` |
| **Movimentação** | Stock movement | Balance change: inbound, outbound, reservation or reversal, with the resulting balance. | `inventory.domain.StockMovement`, `StockMovementType` |
| **Baixa de estoque** | Stock deduction | Outbound of an order's parts when the quote is approved, with the order number as reference. | `StockService.deduct` |
| **Estoque insuficiente** | Insufficient stock | Balance lower than needed; blocks the deduction (and the approval). | `InsufficientStockException` |

## Identity (`identity`)

| Business term (PT) | English term | Meaning | Code |
|---|---|---|---|
| **Usuário** | User | Shop employee with access to the system. | `identity.domain.User` |
| **Perfil** | Role | Administrator (full management) or attendant (registration and service orders). | `identity.domain.Role` (`ADMIN`, `ATTENDANT`) |
| **Token de acesso** | Access token | JWT issued at login, sent in the `Authorization` header. | `AuthenticationService`, `JwtTokenService` |

## Cross-cutting terms (`shared`)

| Business term (PT) | English term | Meaning | Code |
|---|---|---|---|
| **Dinheiro / valor** | Money | Amount in BRL, two decimal places, never negative. | `shared.domain.valueobject.Money` |
| **Evento de domínio** | Domain event | Relevant fact that already happened (e.g. quote approved), published after saving. | `shared.domain.DomainEvent`, `servicing.domain.event.*` |
