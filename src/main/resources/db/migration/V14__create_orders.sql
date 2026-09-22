-- Módulo 1.4 — Pedidos e negociação (RF16–RF21).

-- RF21 — pedidos recorrentes semanais (criada antes de orders, que referencia o recorrente de origem).
CREATE TABLE recurring_orders (
    id                      UUID PRIMARY KEY,
    restaurant_id           UUID NOT NULL REFERENCES restaurants(id),
    delivery_address_id     UUID NOT NULL,
    delivery_day            VARCHAR(10) NOT NULL
                                CHECK (delivery_day IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY',
                                                        'SATURDAY', 'SUNDAY')),
    notes                   VARCHAR(1000),
    status                  VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    next_delivery_date      DATE NOT NULL,
    suspend_after_next_run  BOOLEAN NOT NULL DEFAULT FALSE,
    last_run_at             TIMESTAMP,
    last_run_summary        VARCHAR(2000),
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_recurring_orders_restaurant_id ON recurring_orders(restaurant_id);
CREATE INDEX idx_recurring_orders_status ON recurring_orders(status);

CREATE TABLE recurring_order_items (
    recurring_order_id  UUID NOT NULL REFERENCES recurring_orders(id) ON DELETE CASCADE,
    offer_id            UUID NOT NULL REFERENCES offers(id),
    quantity            NUMERIC(12, 3) NOT NULL CHECK (quantity > 0),
    position            INT NOT NULL,
    PRIMARY KEY (recurring_order_id, offer_id)
);

-- RF16/RF19 — um pedido por produtor; pedidos do mesmo carrinho compartilham checkout_id.
CREATE TABLE orders (
    id                              UUID PRIMARY KEY,
    checkout_id                     UUID NOT NULL,
    restaurant_id                   UUID NOT NULL REFERENCES restaurants(id),
    producer_id                     UUID NOT NULL REFERENCES producers(id),

    -- RF30.2 — cópia do endereço de entrega escolhido no pedido.
    delivery_address_id             UUID,
    delivery_label                  VARCHAR(120),
    delivery_street                 VARCHAR(255) NOT NULL,
    delivery_number                 VARCHAR(20) NOT NULL,
    delivery_neighborhood           VARCHAR(120) NOT NULL,
    delivery_city                   VARCHAR(120) NOT NULL,
    delivery_state                  VARCHAR(2) NOT NULL,
    delivery_zip_code               VARCHAR(10) NOT NULL,
    delivery_complement             VARCHAR(255),
    delivery_latitude               DOUBLE PRECISION,
    delivery_longitude              DOUBLE PRECISION,

    requested_delivery_date         DATE NOT NULL,
    notes                           VARCHAR(1000),
    status                          VARCHAR(20) NOT NULL
                                        CHECK (status IN ('PENDING', 'CONFIRMED', 'IN_PREPARATION', 'IN_TRANSIT',
                                                          'DELIVERED', 'CANCELLED')),
    awaiting_response_from          VARCHAR(20) CHECK (awaiting_response_from IN ('RESTAURANT', 'PRODUCER')),
    total_amount                    NUMERIC(14, 2) NOT NULL,

    placed_at                       TIMESTAMP NOT NULL,
    confirmed_at                    TIMESTAMP,
    preparation_started_at          TIMESTAMP,
    picked_up_at                    TIMESTAMP,
    delivered_at                    TIMESTAMP,
    delivery_auto_confirmed         BOOLEAN NOT NULL DEFAULT FALSE,

    -- RF20/RN10/RN11
    cancelled_by                    VARCHAR(20) CHECK (cancelled_by IN ('RESTAURANT', 'PRODUCER', 'SYSTEM')),
    cancellation_reason             VARCHAR(1000),
    cancelled_at                    TIMESTAMP,
    cancelled_after_confirmation    BOOLEAN,
    penalty_amount                  NUMERIC(14, 2),

    recurring_order_id              UUID REFERENCES recurring_orders(id),

    created_at                      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);
CREATE INDEX idx_orders_producer_id ON orders(producer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_checkout_id ON orders(checkout_id);

-- Itens: dados copiados da oferta + termos vigentes da negociação + rastreabilidade (RN23).
CREATE TABLE order_items (
    id                  UUID PRIMARY KEY,
    order_id            UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    position            INT NOT NULL,
    offer_id            UUID NOT NULL REFERENCES offers(id),
    product_name        VARCHAR(255) NOT NULL,
    category            VARCHAR(30) NOT NULL,
    unit                VARCHAR(20) NOT NULL,
    list_unit_price     NUMERIC(12, 2) NOT NULL,
    quantity            NUMERIC(12, 3) NOT NULL CHECK (quantity > 0),
    unit_price          NUMERIC(12, 2) NOT NULL CHECK (unit_price > 0),
    harvest_date        DATE,
    lot                 VARCHAR(80)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- RF18 — histórico de propostas e contrapropostas.
CREATE TABLE order_negotiation_rounds (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    round_number    INT NOT NULL,
    proposed_by     VARCHAR(20) NOT NULL,
    proposed_at     TIMESTAMP NOT NULL,
    message         VARCHAR(1000),
    delivery_date   DATE,
    UNIQUE (order_id, round_number)
);

CREATE TABLE order_negotiation_terms (
    id              UUID PRIMARY KEY,
    round_id        UUID NOT NULL REFERENCES order_negotiation_rounds(id) ON DELETE CASCADE,
    item_id         UUID NOT NULL,
    quantity        NUMERIC(12, 3) NOT NULL,
    unit_price      NUMERIC(12, 2) NOT NULL
);

-- RF19 — linha do tempo de status.
CREATE TABLE order_status_changes (
    id              UUID PRIMARY KEY,
    order_id        UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    position        INT NOT NULL,
    status          VARCHAR(20) NOT NULL,
    actor           VARCHAR(20) NOT NULL,
    occurred_at     TIMESTAMP NOT NULL,
    note            VARCHAR(1000)
);

CREATE INDEX idx_order_status_changes_order_id ON order_status_changes(order_id);
