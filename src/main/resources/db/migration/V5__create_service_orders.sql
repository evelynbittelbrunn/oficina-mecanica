CREATE SEQUENCE service_order_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE service_orders (
    id                     UUID           PRIMARY KEY,
    number                 VARCHAR(20)    NOT NULL UNIQUE,
    customer_id            UUID           NOT NULL REFERENCES customers (id),
    vehicle_id             UUID           NOT NULL REFERENCES vehicles (id),
    status                 VARCHAR(30)    NOT NULL CHECK (status IN ('RECEIVED', 'IN_DIAGNOSIS',
                               'AWAITING_APPROVAL', 'IN_PROGRESS', 'FINISHED', 'DELIVERED')),
    opened_at              TIMESTAMPTZ    NOT NULL,
    diagnosis_notes        VARCHAR(2000),
    quote_total_amount     NUMERIC(12, 2) CHECK (quote_total_amount >= 0),
    quote_status           VARCHAR(20)    CHECK (quote_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    quote_sent_at          TIMESTAMPTZ,
    quote_decided_at       TIMESTAMPTZ,
    quote_rejection_reason VARCHAR(500),
    version                BIGINT         NOT NULL DEFAULT 0
);

CREATE INDEX idx_service_orders_status ON service_orders (status);
CREATE INDEX idx_service_orders_customer ON service_orders (customer_id);
CREATE INDEX idx_service_orders_opened_at ON service_orders (opened_at DESC);

CREATE TABLE service_order_services (
    id               UUID           PRIMARY KEY,
    service_order_id UUID           NOT NULL REFERENCES service_orders (id) ON DELETE CASCADE,
    service_id       UUID           NOT NULL REFERENCES services (id),
    name             VARCHAR(150)   NOT NULL,
    unit_price       NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    quantity         INTEGER        NOT NULL CHECK (quantity > 0),
    position         INTEGER
);

CREATE TABLE service_order_parts (
    id                UUID           PRIMARY KEY,
    service_order_id  UUID           NOT NULL REFERENCES service_orders (id) ON DELETE CASCADE,
    inventory_item_id UUID           NOT NULL REFERENCES inventory_items (id),
    name              VARCHAR(150)   NOT NULL,
    unit_price        NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    quantity          INTEGER        NOT NULL CHECK (quantity > 0),
    position          INTEGER
);

CREATE TABLE service_order_status_history (
    id               UUID         PRIMARY KEY,
    service_order_id UUID         NOT NULL REFERENCES service_orders (id) ON DELETE CASCADE,
    previous_status  VARCHAR(30),
    new_status       VARCHAR(30)  NOT NULL,
    changed_at       TIMESTAMPTZ  NOT NULL,
    actor            VARCHAR(150) NOT NULL,
    note             VARCHAR(600),
    position         INTEGER
);

CREATE INDEX idx_service_order_services_order ON service_order_services (service_order_id);
CREATE INDEX idx_service_order_parts_order ON service_order_parts (service_order_id);
CREATE INDEX idx_service_order_history_order ON service_order_status_history (service_order_id, new_status);
