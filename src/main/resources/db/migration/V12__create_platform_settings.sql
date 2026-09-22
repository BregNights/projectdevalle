-- RF43 — parâmetros globais da plataforma, configuráveis pela administração.
CREATE TABLE platform_settings (
    id                          SMALLINT PRIMARY KEY CHECK (id = 1),
    -- RN13 — comissão percentual retida sobre cada transação concluída.
    commission_percentage       NUMERIC(5, 2) NOT NULL CHECK (commission_percentage >= 0 AND commission_percentage < 100),
    enabled_product_categories  TEXT[] NOT NULL,
    updated_at                  TIMESTAMP NOT NULL DEFAULT now()
);

-- RN02 — municípios atendidos, agrupados por região (usados na aprovação e no filtro "região" do catálogo).
CREATE TABLE platform_coverage_cities (
    region          VARCHAR(120) NOT NULL,
    region_position INT NOT NULL,
    city            VARCHAR(120) NOT NULL,
    city_position   INT NOT NULL,
    PRIMARY KEY (region, city)
);

INSERT INTO platform_settings (id, commission_percentage, enabled_product_categories)
VALUES (1, 10.00, ARRAY['VEGETABLES', 'FRUITS', 'FISH', 'MEAT_POULTRY', 'DAIRY', 'GRAINS_CEREALS', 'PROCESSED',
                        'OTHER']);

-- Área de cobertura inicial (antes fixa no application.yaml): Vale do Itajaí e litoral norte de SC.
INSERT INTO platform_coverage_cities (region, region_position, city, city_position) VALUES
    ('Vale do Itajaí', 0, 'Blumenau', 0),
    ('Vale do Itajaí', 0, 'Brusque', 1),
    ('Vale do Itajaí', 0, 'Gaspar', 2),
    ('Vale do Itajaí', 0, 'Indaial', 3),
    ('Vale do Itajaí', 0, 'Timbó', 4),
    ('Vale do Itajaí', 0, 'Pomerode', 5),
    ('Vale do Itajaí', 0, 'Ilhota', 6),
    ('Litoral Norte', 1, 'Itajaí', 0),
    ('Litoral Norte', 1, 'Navegantes', 1),
    ('Litoral Norte', 1, 'Balneário Camboriú', 2),
    ('Litoral Norte', 1, 'Camboriú', 3),
    ('Litoral Norte', 1, 'Itapema', 4),
    ('Litoral Norte', 1, 'Penha', 5),
    ('Litoral Norte', 1, 'Piçarras', 6),
    ('Litoral Norte', 1, 'Barra Velha', 7),
    ('Litoral Norte', 1, 'São Francisco do Sul', 8),
    ('Litoral Norte', 1, 'Joinville', 9);
