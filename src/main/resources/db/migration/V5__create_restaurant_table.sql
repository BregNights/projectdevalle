CREATE TABLE restaurants (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id),
    corporate_name  VARCHAR(255) NOT NULL,
    cnpj            VARCHAR(20) NOT NULL,
    category        VARCHAR(30) NOT NULL
                        CHECK (category IN ('FINE_DINING', 'BISTRO', 'CHAIN', 'OTHER')),

    contact_name    VARCHAR(255) NOT NULL,
    contact_role    VARCHAR(120),
    contact_phone   VARCHAR(30),
    contact_email   VARCHAR(255),

    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED')),

    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now()
);
