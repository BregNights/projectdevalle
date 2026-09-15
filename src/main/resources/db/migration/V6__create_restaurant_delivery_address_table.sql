CREATE TABLE restaurant_delivery_addresses (
    id              UUID PRIMARY KEY,
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id),
    label           VARCHAR(100) NOT NULL,
    street          VARCHAR(255) NOT NULL,
    number          VARCHAR(20) NOT NULL,
    neighborhood    VARCHAR(120) NOT NULL,
    city            VARCHAR(120) NOT NULL,
    state           VARCHAR(2) NOT NULL,
    zip_code        VARCHAR(10) NOT NULL,
    complement      VARCHAR(255),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_restaurant_delivery_addresses_restaurant_id ON restaurant_delivery_addresses(restaurant_id);
</content>
