-- =============================================================
-- Devicefy - V3: Despliegues (misiones de maquetación / renove)
-- Fase 6: importación de Excel
-- =============================================================

-- -----------------------------------------------------------------
-- Entidades del sistema origen (jerarquía tipo GLPI: Root entity > Informàtica)
-- -----------------------------------------------------------------
CREATE TABLE entidades (
    id            BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(150) NOT NULL,
    entidad_raiz  VARCHAR(150),
    activo        BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_entidades_nombre UNIQUE (nombre)
);

-- -----------------------------------------------------------------
-- Despliegues (misiones: Girona, Tarragona, Terres de l'Ebre, ...)
-- -----------------------------------------------------------------
CREATE TABLE despliegues (
    id                BIGSERIAL PRIMARY KEY,
    nombre            VARCHAR(150) NOT NULL,
    fichero_nombre    VARCHAR(255),
    fecha_importacion TIMESTAMP,
    estado            VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_despliegues_nombre UNIQUE (nombre)
);

-- -----------------------------------------------------------------
-- Equipos de un despliegue (misión): estado del renove por equipo
-- -----------------------------------------------------------------
CREATE TABLE despliegue_equipos (
    id                BIGSERIAL PRIMARY KEY,
    despliegue_id     BIGINT NOT NULL REFERENCES despliegues (id) ON DELETE CASCADE,
    equipo_id         BIGINT NOT NULL REFERENCES equipos (id) ON DELETE CASCADE,
    hostname_actual   VARCHAR(100),
    hostname_nuevo    VARCHAR(100),
    estado_renove     VARCHAR(10),
    anio_renove       INT,
    perfil_imagen     VARCHAR(50),
    tecnico_id        BIGINT REFERENCES usuarios (id) ON DELETE SET NULL,
    fecha_toma        TIMESTAMP,
    estado            VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_despliegue_equipo UNIQUE (despliegue_id, equipo_id),
    CONSTRAINT chk_de_renove CHECK (estado_renove IN ('R', 'E', 'H')),
    CONSTRAINT chk_de_estado CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'HECHO', 'CANCELADO'))
);

CREATE INDEX idx_despliegue_equipos_despliegue ON despliegue_equipos (despliegue_id);
CREATE INDEX idx_despliegue_equipos_equipo ON despliegue_equipos (equipo_id);

-- -----------------------------------------------------------------
-- Los centros procedentes de la importación se vinculan a una entidad
-- -----------------------------------------------------------------
ALTER TABLE centros ADD COLUMN entidad_id BIGINT REFERENCES entidades (id) ON DELETE SET NULL;
