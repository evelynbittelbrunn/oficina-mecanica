ALTER TABLE vehicles ADD CONSTRAINT ck_vehicles_year CHECK (year >= 1900);
CREATE INDEX idx_vehicles_customer ON vehicles (customer_id);

CREATE TABLE users (
    id            UUID         PRIMARY KEY,
    name          VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'ATTENDANT')),
    active        BOOLEAN      NOT NULL DEFAULT TRUE
);
