-- =============================================================
-- Devicefy - V6: CRUD Proyectos + Centros ampliados + Responsables
-- - Proyectos (despliegues) creados a mano con Comunidad Autónoma
--   y multiselección de centros
-- - Centros: Comunidad Autónoma, Provincia, Teléfono, e-mail
-- - Responsables por centro (1..N)
-- =============================================================

-- 1. Centros ampliados
ALTER TABLE centros ADD COLUMN comunidad_autonoma VARCHAR(100);
ALTER TABLE centros ADD COLUMN provincia        VARCHAR(100);
ALTER TABLE centros ADD COLUMN telefono         VARCHAR(30);
ALTER TABLE centros ADD COLUMN email            VARCHAR(150);

CREATE INDEX idx_centros_comunidad_autonoma ON centros (comunidad_autonoma);

-- 2. Responsables por centro
CREATE TABLE centro_responsables (
    id            BIGSERIAL PRIMARY KEY,
    centro_id     BIGINT NOT NULL REFERENCES centros (id) ON DELETE CASCADE,
    area_oficina  VARCHAR(100),
    nombre        VARCHAR(120),
    telefono      VARCHAR(30),
    email         VARCHAR(150),
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_centro_responsables_centro ON centro_responsables (centro_id);

-- 3. Proyecto -> Comunidad Autónoma
ALTER TABLE despliegues ADD COLUMN comunidad_autonoma VARCHAR(100);

-- 4. Proyecto <-> Centros (multiselección filtrada por Comunidad Autónoma)
CREATE TABLE despliegue_centros (
    despliegue_id BIGINT NOT NULL REFERENCES despliegues (id) ON DELETE CASCADE,
    centro_id     BIGINT NOT NULL REFERENCES centros (id) ON DELETE CASCADE,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_despliegue_centros PRIMARY KEY (despliegue_id, centro_id)
);

CREATE INDEX idx_despliegue_centros_centro ON despliegue_centros (centro_id);
