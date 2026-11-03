# Event Storming

Text base for the Miro board. Event Storming color convention:

| Sticky note | Color | Meaning |
|---|---|---|
| Event | 🟧 orange | Something that happened, in the past tense (e.g. *Quote sent*) |
| Command | 🟦 blue | An actor's intention (e.g. *Approve quote*) |
| Actor | 🟨 yellow | Who triggers the command |
| Policy | 🟪 lilac | Reactive rule: "when *event*, then *command*" |
| Aggregate | 🟨 light yellow | Who decides and protects the rules |
| Read model | 🟩 green | Information consulted to decide |
| External system | 🩷 pink | Integration outside the system |
| Hotspot | 🟥 red | Question, risk or rule to confirm |

---

## Flow 1 — Opening a service order

```mermaid
flowchart LR
    A1(["🟨 Attendant"]) --> C1["🟦 Open service order<br/>(document + plate + items)"]
    RM1[/"🟩 Customer by document<br/>Vehicle by plate<br/>Active services · Inventory items"/] -.-> C1
    C1 --> AG1{{"🟨 Service Order"}}
    AG1 --> E1["🟧 Vehicle registered<br/>(if new plate)"]
    AG1 --> E2["🟧 Service order opened<br/>(RECEIVED)"]
    E2 --> C2["🟦 Start diagnosis"]
    M1(["🟨 Mechanic"]) --> C2
    C2 --> E3["🟧 Diagnosis started"]
    E3 --> C3["🟦 Add services and parts"]
    C3 --> E4["🟧 Items added<br/>(current price)"]
    E4 --> C4["🟦 Complete diagnosis"]
    C4 --> E5["🟧 Quote generated"]
    E5 --> P1["🟪 When quote generated,<br/>send it to the customer"]
    P1 --> E6["🟧 Quote sent<br/>(AWAITING_APPROVAL)"]
    E6 --> X1["🩷 Customer notification<br/>(e-mail/SMS — simulated)"]
```

| # | Actor | Command | Aggregate | Event(s) | Rules / hotspots |
|---|---|---|---|---|---|
| 1 | Attendant | Open order | Service Order (+ Vehicle) | *Vehicle registered*, *Service order opened* | Customer must be registered; plate of another customer → refused (422) |
| 2 | Mechanic | Start diagnosis | Service Order | *Diagnosis started* | Only from RECEIVED |
| 3 | Mechanic/Attendant | Add/remove service or part | Service Order | *Items added/removed* | Only in RECEIVED/IN_DIAGNOSIS; inactive service → refused |
| 4 | Mechanic | Complete diagnosis | Service Order | *Quote generated*, *Quote sent* | Requires ≥ 1 service; 🟥 a part without stock does not block the quote (only the approval) |

Code: `ServiceOrderService.open`, `addService`, `addPart`, `startDiagnosis`, `completeDiagnosis`;
`CustomerNotificationListener.notifyQuoteSent`.

---

## Flow 2 — Tracking the service order (customer and shop)

```mermaid
flowchart LR
    CU(["🟨 Customer"]) --> C1["🟦 Track order<br/>(number + document)"]
    C1 -.-> RM1[/"🟩 Status, items,<br/>quote and history"/]
    CU --> C2["🟦 Approve quote"]
    CU --> C3["🟦 Reject quote<br/>(with reason)"]
    C2 --> AG1{{"🟨 Service Order"}}
    C3 --> AG1
    AG1 --> E1["🟧 Quote approved"]
    E1 --> P1["🟪 When approved,<br/>deduct parts from stock<br/>(same transaction)"]
    P1 --> E2["🟧 Parts deducted"]
    E2 --> E3["🟧 Execution started<br/>(IN_PROGRESS)"]
    P1 -. "no balance" .-> E9["🟧 Approval refused<br/>(insufficient stock)"]
    AG1 --> E4["🟧 Quote rejected<br/>(FINISHED without execution)"]
    M1(["🟨 Mechanic"]) --> C4["🟦 Finish execution"]
    C4 --> E5["🟧 Execution finished<br/>(FINISHED)"]
    E4 --> C5
    E5 --> C5["🟦 Deliver vehicle"]
    A1(["🟨 Attendant"]) --> C5
    C5 --> E6["🟧 Vehicle delivered<br/>(DELIVERED)"]
    E6 --> P2["🟪 When delivered,<br/>notify customer"]
    E5 -.-> RM2[/"🟩 Average execution time<br/>(overall and per service)"/]
```

| # | Actor | Command | Event(s) | Rules / hotspots |
|---|---|---|---|---|
| 1 | Customer | Track order | — | Number + document must match; otherwise "not found" (404) |
| 2 | Customer | Approve quote | *Quote approved*, *Parts deducted*, *Execution started* | Only in AWAITING_APPROVAL; missing stock refuses everything (422) |
| 3 | Customer | Reject quote | *Quote rejected* | Reason required; does not move stock |
| 4 | Mechanic | Finish execution | *Execution finished* | Only in IN_PROGRESS |
| 5 | Attendant | Deliver vehicle | *Vehicle delivered* | Only in FINISHED |
| — | Administrator | Check average time | — | Read model from the history; 🟥 rejected orders are excluded |

Code: `CustomerTrackingService.track/approveQuote/rejectQuote`, `ServiceOrderService.finish/deliver`,
`ReportService.averageExecutionTime`.

---

## Flow 3 — Parts and supplies management

```mermaid
flowchart LR
    AD(["🟨 Administrator"]) --> C1["🟦 Register part/supply<br/>(with initial balance)"]
    C1 --> AG1{{"🟨 Inventory Item"}}
    AG1 --> E1["🟧 Item registered"]
    E1 --> E2["🟧 Stock-in registered<br/>(initial stock)"]
    AD --> C2["🟦 Register stock-in<br/>(purchase/receipt)"]
    C2 --> AG1
    AG1 --> E3["🟧 Stock-in registered"]
    AD --> C3["🟦 Register stock-out<br/>(loss/internal use)"]
    C3 --> AG1
    AG1 --> E4["🟧 Stock-out registered"]
    EXT["🟧 Quote approved<br/>(Flow 2)"] --> P1["🟪 Deduct the order's parts"]
    P1 --> AG1
    AG1 --> E5["🟧 Parts deducted<br/>(OUTBOUND ref. order number)"]
    E4 --> P2["🟪 When balance ≤ minimum,<br/>flag restocking"]
    E5 --> P2
    P2 --> E6["🟧 Low stock identified"]
    E6 -.-> RM1[/"🟩 Low-stock list"/]
    AG1 -.-> RM2[/"🟩 Movement history"/]
```

| # | Actor | Command | Event(s) | Rules / hotspots |
|---|---|---|---|---|
| 1 | Administrator | Register part/supply | *Item registered*, *Stock-in registered* | Unique code; initial balance becomes an INBOUND movement |
| 2 | Administrator | Register stock-in | *Stock-in registered* | Quantity > 0 |
| 3 | Administrator | Register stock-out | *Stock-out registered* | Cannot make the balance negative (422) |
| 4 | System | Deduct the order's parts | *Parts deducted* | Validates every item before deducting; concurrency via version (409) |
| 5 | System | Flag restocking | *Low stock identified* | Balance ≤ minimum; 🟥 reserving parts when added to the order is an evolution (RESERVATION/REVERSAL) |

Code: `InventoryItemService.createItem/registerStockIn/registerStockOut/listLowStockItems`,
`StockService.deduct`, `InventoryItem.move`.
