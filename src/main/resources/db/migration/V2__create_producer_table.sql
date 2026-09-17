CREATE TABLE producers (
    id                      UUID PRIMARY KEY,
    user_id                 UUID NOT NULL REFERENCES users(id),
    name                    VARCHAR(255) NOT NULL,
    document_type           VARCHAR(10) NOT NULL
                                CHECK (document_type IN ('CPF', 'CNPJ')),
    document_number         VARCHAR(20) NOT NULL,
    production_type         VARCHAR(30) NOT NULL
                                CHECK (production_type IN ('FARMING', 'FISHING', 'LIVESTOCK', 'ARTISANAL_PROCESSING')),

    origin_street           VARCHAR(255) NOT NULL,
    origin_number           VARCHAR(20) NOT NULL,
    origin_neighborhood     VARCHAR(120) NOT NULL,
    origin_city             VARCHAR(120) NOT NULL,
    origin_state            VARCHAR(2) NOT NULL,
    origin_zip_code         VARCHAR(10) NOT NULL,
    origin_complement       VARCHAR(255),
    origin_latitude         DOUBLE PRECISION,
    origin_longitude        DOUBLE PRECISION,
    geocoding_pending       BOOLEAN NOT NULL DEFAULT FALSE,

    bank_name               VARCHAR(120),
    bank_agency             VARCHAR(20),
    bank_account            VARCHAR(30),
    bank_account_type       VARCHAR(20),
    bank_account_holder     VARCHAR(255),

    delivery_area           TEXT[] NOT NULL DEFAULT '{}',

    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                                CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),

    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now()
);
