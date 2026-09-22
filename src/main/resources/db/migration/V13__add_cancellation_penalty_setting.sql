-- RN10/RF43 — multa proporcional cobrada do restaurante que cancela depois de o produtor iniciar o preparo.
ALTER TABLE platform_settings
    ADD COLUMN cancellation_penalty_percentage NUMERIC(5, 2) NOT NULL DEFAULT 20.00
        CHECK (cancellation_penalty_percentage >= 0 AND cancellation_penalty_percentage < 100);
