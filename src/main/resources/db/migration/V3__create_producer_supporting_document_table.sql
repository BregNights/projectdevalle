CREATE TABLE producer_supporting_documents (
    id              UUID PRIMARY KEY,
    producer_id     UUID NOT NULL REFERENCES producers(id),
    document_type   VARCHAR(30) NOT NULL
                        CHECK (document_type IN ('CPF', 'CNPJ', 'DAP_CAF', 'FISHING_LICENSE')),
    document_number VARCHAR(50) NOT NULL,
    file_url        VARCHAR(500) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_producer_supporting_documents_producer_id ON producer_supporting_documents(producer_id);
