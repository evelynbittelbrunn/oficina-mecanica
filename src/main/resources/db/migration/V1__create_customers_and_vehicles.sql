CREATE TABLE customers (
    id       UUID         PRIMARY KEY,
    name     VARCHAR(150) NOT NULL,
    document VARCHAR(14)  NOT NULL UNIQUE,
    email    VARCHAR(150) NOT NULL,
    phone    VARCHAR(11)  NOT NULL
);

CREATE TABLE vehicles (
    id            UUID         PRIMARY KEY,
    customer_id   UUID         NOT NULL REFERENCES customers (id),
    license_plate VARCHAR(7)   NOT NULL UNIQUE,
    brand         VARCHAR(100) NOT NULL,
    model         VARCHAR(100) NOT NULL,
    year          INTEGER      NOT NULL
);
