CREATE TABLE producer_certifications (
    id              UUID PRIMARY KEY,
    producer_id     UUID NOT NULL REFERENCES producers(id),
    type            VARCHAR(30) NOT NULL
                        CHECK (type IN ('ORGANIC', 'ORIGIN_SEAL', 'GOOD_FISHING_PRACTICES', 'OTHER')),
    proof_url       VARCHAR(500),
    valid_until     DATE,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_producer_certifications_producer_id ON producer_certifications(producer_id);
