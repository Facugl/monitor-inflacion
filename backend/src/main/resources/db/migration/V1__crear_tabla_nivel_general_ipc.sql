CREATE TABLE nivel_general_ipc (
    id         BIGSERIAL PRIMARY KEY,
    periodo    VARCHAR(6)     NOT NULL,
    region     VARCHAR(20)    NOT NULL,
    serie      VARCHAR(20)    NOT NULL,
    indice     NUMERIC(12, 4) NOT NULL,
    CONSTRAINT uk_nivel_general_ipc_periodo_region_serie UNIQUE (periodo, region, serie)
);

CREATE INDEX idx_nivel_general_ipc_region_serie_periodo
    ON nivel_general_ipc (region, serie, periodo);

COMMENT ON TABLE nivel_general_ipc IS
    'Índice del nivel general del IPC, sincronizado desde la API pública del INDEC (/ipc_nacional).';
