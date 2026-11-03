CREATE TABLE inventory_items (
    id             UUID           PRIMARY KEY,
    code           VARCHAR(40)    NOT NULL UNIQUE,
    name           VARCHAR(150)   NOT NULL,
    type           VARCHAR(10)    NOT NULL CHECK (type IN ('PART', 'SUPPLY')),
    unit_price     NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    stock_quantity INTEGER        NOT NULL CHECK (stock_quantity >= 0),
    minimum_stock  INTEGER        NOT NULL CHECK (minimum_stock >= 0),
    version        BIGINT         NOT NULL DEFAULT 0
);

CREATE TABLE stock_movements (
    id            UUID         PRIMARY KEY,
    item_id       UUID         NOT NULL REFERENCES inventory_items (id) ON DELETE CASCADE,
    type          VARCHAR(12)  NOT NULL CHECK (type IN ('INBOUND', 'OUTBOUND', 'RESERVATION', 'REVERSAL')),
    quantity      INTEGER      NOT NULL CHECK (quantity > 0),
    balance_after INTEGER      NOT NULL CHECK (balance_after >= 0),
    reference     VARCHAR(255),
    occurred_at   TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_stock_movements_item ON stock_movements (item_id, occurred_at DESC);
