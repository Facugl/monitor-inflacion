CREATE TABLE indice_categoria_ipc (
    id                    BIGSERIAL PRIMARY KEY,
    periodo               VARCHAR(6)     NOT NULL,
    region                VARCHAR(20)    NOT NULL,
    categoria_codigo      VARCHAR(10)    NOT NULL,
    categoria_descripcion VARCHAR(200)   NOT NULL,
    indice                NUMERIC(12, 4) NOT NULL,
    CONSTRAINT uk_indice_categoria_ipc_periodo_region_categoria
        UNIQUE (periodo, region, categoria_codigo)
);

-- El ranking consulta siempre por región y rango de períodos, trayendo todas las
-- categorías; este índice cubre exactamente ese acceso.
CREATE INDEX idx_indice_categoria_ipc_region_periodo
    ON indice_categoria_ipc (region, periodo);

COMMENT ON TABLE indice_categoria_ipc IS
    'Índice del IPC abierto por división COICOP (12 categorías), sincronizado desde la API pública del INDEC (/ipc_nacional, nivel=División).';

COMMENT ON COLUMN indice_categoria_ipc.categoria_descripcion IS
    'Descripción publicada por el INDEC. Se guarda junto al código porque la redacción la define un tercero y puede cambiar.';
