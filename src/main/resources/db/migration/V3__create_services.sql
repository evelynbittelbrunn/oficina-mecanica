CREATE TABLE services (
    id                UUID           PRIMARY KEY,
    name              VARCHAR(120)   NOT NULL,
    description       VARCHAR(500),
    base_price        NUMERIC(12, 2) NOT NULL CHECK (base_price >= 0),
    estimated_minutes INTEGER        NOT NULL CHECK (estimated_minutes > 0),
    active            BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_services_active ON services (active);
