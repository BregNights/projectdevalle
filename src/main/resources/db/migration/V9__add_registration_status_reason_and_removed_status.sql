-- RF40 — remoção de cadastro pela administração (novo status REMOVED).
ALTER TABLE producers DROP CONSTRAINT producers_status_check;
ALTER TABLE producers ADD CONSTRAINT producers_status_check
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED', 'REMOVED'));

ALTER TABLE restaurants DROP CONSTRAINT restaurants_status_check;
ALTER TABLE restaurants ADD CONSTRAINT restaurants_status_check
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED', 'REMOVED'));

-- RN29 — motivo informado pelo administrador na rejeição, suspensão ou remoção.
ALTER TABLE producers ADD COLUMN status_reason VARCHAR(1000);
ALTER TABLE restaurants ADD COLUMN status_reason VARCHAR(1000);
