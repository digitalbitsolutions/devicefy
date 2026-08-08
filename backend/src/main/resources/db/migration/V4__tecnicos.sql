-- =============================================================
-- Devicefy - V4: asignación de centros a técnicos
-- Fase 7: workflow del técnico
-- =============================================================

CREATE TABLE usuario_centros (
    usuario_id BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    centro_id  BIGINT NOT NULL REFERENCES centros (id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, centro_id)
);
