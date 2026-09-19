CREATE TABLE offers (
    id                      UUID PRIMARY KEY,
    producer_id             UUID NOT NULL REFERENCES producers(id),
    product_name            VARCHAR(255) NOT NULL,
    category                VARCHAR(30) NOT NULL
                                CHECK (category IN ('VEGETABLES', 'FRUITS', 'FISH', 'MEAT_POULTRY', 'DAIRY',
                                                     'GRAINS_CEREALS', 'PROCESSED', 'OTHER')),
    unit                    VARCHAR(20) NOT NULL
                                CHECK (unit IN ('KILOGRAM', 'GRAM', 'LITER', 'UNIT', 'DOZEN', 'BOX')),
    price                   NUMERIC(12, 2) NOT NULL,
    quantity_available      NUMERIC(12, 3) NOT NULL,

    recurrence_type         VARCHAR(20) NOT NULL
                                CHECK (recurrence_type IN ('RECURRING', 'ONE_TIME')),
    recurrence_day_of_week  VARCHAR(10)
                                CHECK (recurrence_day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY',
                                                                   'FRIDAY', 'SATURDAY', 'SUNDAY')),

    availability_from       DATE,
    availability_until      DATE,

    photo_urls              TEXT[] NOT NULL DEFAULT '{}',

    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE', 'PAUSED', 'SOLD_OUT', 'REMOVED')),

    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_offers_producer_id ON offers(producer_id);
CREATE INDEX idx_offers_status ON offers(status);
CREATE INDEX idx_offers_category ON offers(category);
