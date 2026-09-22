-- RF04/RF07 — metadados dos arquivos enviados (fotos de oferta, documentos do cadastro, comprovantes de
-- certificação). O conteúdo fica no armazenamento de arquivos, identificado pelo id.
CREATE TABLE stored_files (
    id              UUID PRIMARY KEY,
    purpose         VARCHAR(30) NOT NULL
                        CHECK (purpose IN ('OFFER_PHOTO', 'SUPPORTING_DOCUMENT', 'CERTIFICATION_PROOF')),
    file_type       VARCHAR(10) NOT NULL CHECK (file_type IN ('PDF', 'JPEG', 'PNG', 'WEBP')),
    size_bytes      BIGINT NOT NULL CHECK (size_bytes > 0),
    original_name   VARCHAR(255) NOT NULL,
    uploaded_by     UUID REFERENCES users(id),
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_stored_files_uploaded_by ON stored_files(uploaded_by);
