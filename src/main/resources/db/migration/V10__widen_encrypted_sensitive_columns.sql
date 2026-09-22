-- RNF06 — documentos pessoais e dados bancários passam a ser gravados criptografados (AES-256-GCM, Base64),
-- o que ocupa mais espaço que o texto puro.
ALTER TABLE producers ALTER COLUMN document_number TYPE VARCHAR(512);
ALTER TABLE producers ALTER COLUMN bank_name TYPE VARCHAR(512);
ALTER TABLE producers ALTER COLUMN bank_agency TYPE VARCHAR(512);
ALTER TABLE producers ALTER COLUMN bank_account TYPE VARCHAR(512);
ALTER TABLE producers ALTER COLUMN bank_account_holder TYPE VARCHAR(1024);
ALTER TABLE producer_supporting_documents ALTER COLUMN document_number TYPE VARCHAR(512);
ALTER TABLE restaurants ALTER COLUMN cnpj TYPE VARCHAR(512);
