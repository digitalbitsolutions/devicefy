-- =============================================================
-- Devicefy - V5: Dashboard Admin
-- - Provincia/territorio del proyecto (despliegue)
-- - Asignación de proyectos (despliegues) a técnicos
-- =============================================================

ALTER TABLE despliegues ADD COLUMN provincia VARCHAR(100);

-- Asignación de proyectos a usuarios (técnicos)
CREATE TABLE despliegue_tecnicos (
    despliegue_id BIGINT NOT NULL REFERENCES despliegues (id) ON DELETE CASCADE,
    usuario_id    BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_despliegue_tecnicos PRIMARY KEY (despliegue_id, usuario_id)
);

CREATE INDEX idx_despliegue_tecnicos_usuario ON despliegue_tecnicos (usuario_id);

-- Rellenar la provincia de los proyectos existentes a partir de su nombre
UPDATE despliegues SET provincia = nombre WHERE provincia IS NULL;
