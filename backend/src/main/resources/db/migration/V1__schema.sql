-- =============================================================
-- Devicefy - V1: Esquema base
-- Inventario de equipos e intervenciones (fases 2-3)
-- Principio de historial: las intervenciones son de solo lectura;
-- la ficha actual del equipo se actualiza por separado.
-- =============================================================

-- -----------------------------------------------------------------
-- Usuarios de la aplicación (técnicos) y roles
-- -----------------------------------------------------------------
CREATE TABLE usuarios (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    email           VARCHAR(150),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uq_usuarios_username UNIQUE (username)
);

CREATE TABLE roles (
    id     BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT uq_roles_nombre UNIQUE (nombre)
);

CREATE TABLE usuarios_roles (
    usuario_id BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    rol_id     BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);

-- -----------------------------------------------------------------
-- Centros y ubicaciones
-- -----------------------------------------------------------------
CREATE TABLE centros (
    id         BIGSERIAL PRIMARY KEY,
    codigo     VARCHAR(20) NOT NULL,
    nombre     VARCHAR(150) NOT NULL,
    tipo       VARCHAR(30),
    direccion  VARCHAR(255),
    activo     BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_centros_codigo UNIQUE (codigo)
);

CREATE TABLE ubicaciones (
    id        BIGSERIAL PRIMARY KEY,
    centro_id BIGINT NOT NULL REFERENCES centros (id) ON DELETE RESTRICT,
    nombre    VARCHAR(150) NOT NULL,
    planta    VARCHAR(50),
    zona      VARCHAR(50),
    activo    BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_ubicaciones_centro_nombre UNIQUE (centro_id, nombre)
);

-- -----------------------------------------------------------------
-- Personas que usan los equipos (distintas de los técnicos)
-- -----------------------------------------------------------------
CREATE TABLE usuarios_asignados (
    id         BIGSERIAL PRIMARY KEY,
    nombre     VARCHAR(150) NOT NULL,
    puesto     VARCHAR(150),
    email      VARCHAR(150),
    telefono   VARCHAR(30),
    activo     BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- -----------------------------------------------------------------
-- Catálogo de estados del equipo (configurable: P/I/U se definen
-- aquí sin cambios de esquema)
-- -----------------------------------------------------------------
CREATE TABLE estados_equipo (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(30) NOT NULL,
    nombre      VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255),
    orden       INT         NOT NULL DEFAULT 0,
    activo      BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_estados_equipo_codigo UNIQUE (codigo)
);

-- -----------------------------------------------------------------
-- Equipos (CPU / estaciones de trabajo)
-- -----------------------------------------------------------------
CREATE TABLE equipos (
    id                   BIGSERIAL PRIMARY KEY,
    hostname             VARCHAR(100),
    numero_serie         VARCHAR(100),
    etiqueta_patrimonial VARCHAR(100),
    fabricante           VARCHAR(100),
    modelo               VARCHAR(100),
    sistema_operativo    VARCHAR(100),
    procesador           VARCHAR(150),
    tipo_equipo          VARCHAR(30) NOT NULL DEFAULT 'CPU',
    estado               VARCHAR(30),
    centro_id            BIGINT REFERENCES centros (id) ON DELETE RESTRICT,
    ubicacion_id         BIGINT REFERENCES ubicaciones (id) ON DELETE RESTRICT,
    usuario_asignado_id  BIGINT REFERENCES usuarios_asignados (id) ON DELETE SET NULL,
    observaciones        TEXT,
    activo               BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_equipos_hostname UNIQUE (hostname),
    CONSTRAINT uq_equipos_serie UNIQUE (numero_serie),
    CONSTRAINT uq_equipos_etiqueta UNIQUE (etiqueta_patrimonial),
    CONSTRAINT fk_equipos_estado FOREIGN KEY (estado) REFERENCES estados_equipo (codigo) ON DELETE SET NULL,
    CONSTRAINT chk_equipos_tipo CHECK (tipo_equipo IN ('CPU', 'ESTACION_TRABAJO', 'PORTATIL', 'SERVIDOR', 'THIN_CLIENT'))
);

CREATE INDEX idx_equipos_centro ON equipos (centro_id);
CREATE INDEX idx_equipos_ubicacion ON equipos (ubicacion_id);
CREATE INDEX idx_equipos_estado ON equipos (estado);

-- -----------------------------------------------------------------
-- Configuración de red (1:1 con el equipo)
-- -----------------------------------------------------------------
CREATE TABLE redes (
    id              BIGSERIAL PRIMARY KEY,
    equipo_id       BIGINT NOT NULL REFERENCES equipos (id) ON DELETE CASCADE,
    tipo_asignacion VARCHAR(20) NOT NULL DEFAULT 'DHCP',
    ip              VARCHAR(45),
    mascara         VARCHAR(45),
    puerta_enlace   VARCHAR(45),
    dns1            VARCHAR(45),
    dns2            VARCHAR(45),
    dominio         VARCHAR(150),
    actualizada_at  TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_redes_equipo UNIQUE (equipo_id),
    CONSTRAINT uq_redes_ip UNIQUE (ip),
    CONSTRAINT chk_redes_tipo CHECK (tipo_asignacion IN ('DHCP', 'ESTATICA'))
);

-- -----------------------------------------------------------------
-- Periféricos (monitores, impresoras, etc.)
-- -----------------------------------------------------------------
CREATE TABLE perifericos (
    id                   BIGSERIAL PRIMARY KEY,
    equipo_id            BIGINT REFERENCES equipos (id) ON DELETE SET NULL,
    tipo                 VARCHAR(30) NOT NULL,
    marca                VARCHAR(100),
    modelo               VARCHAR(100),
    numero_serie         VARCHAR(100),
    etiqueta_patrimonial VARCHAR(100),
    tamanio_pulgadas     NUMERIC(4, 1),
    activo               BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMP NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_perifericos_tipo CHECK (tipo IN ('MONITOR', 'IMPRESORA', 'TECLADO', 'RATON', 'OTRO'))
);

CREATE INDEX idx_perifericos_equipo ON perifericos (equipo_id);

-- -----------------------------------------------------------------
-- Catálogo de software a comprobar / instalar
-- -----------------------------------------------------------------
CREATE TABLE software (
    id                 BIGSERIAL PRIMARY KEY,
    nombre             VARCHAR(150) NOT NULL,
    fabricante         VARCHAR(150),
    version_referencia VARCHAR(50),
    categoria          VARCHAR(80),
    activo             BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_software_nombre UNIQUE (nombre)
);

-- -----------------------------------------------------------------
-- Intervenciones (historial inmutable)
-- -----------------------------------------------------------------
CREATE TABLE intervenciones (
    id            BIGSERIAL PRIMARY KEY,
    equipo_id     BIGINT NOT NULL REFERENCES equipos (id) ON DELETE RESTRICT,
    tecnico_id    BIGINT NOT NULL REFERENCES usuarios (id) ON DELETE RESTRICT,
    tipo          VARCHAR(30) NOT NULL,
    estado        VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
    fecha_inicio  TIMESTAMP NOT NULL DEFAULT now(),
    fecha_fin     TIMESTAMP,
    observaciones TEXT,
    incidencias   TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_intervenciones_tipo CHECK (tipo IN ('INSTALACION', 'MANTENIMIENTO', 'REPARACION', 'ACTUALIZACION', 'INVENTARIO')),
    CONSTRAINT chk_intervenciones_estado CHECK (estado IN ('BORRADOR', 'EN_PROGRESO', 'FINALIZADA', 'CANCELADA'))
);

CREATE INDEX idx_intervenciones_equipo ON intervenciones (equipo_id);
CREATE INDEX idx_intervenciones_tecnico ON intervenciones (tecnico_id);
CREATE INDEX idx_intervenciones_fecha ON intervenciones (fecha_inicio);

-- -----------------------------------------------------------------
-- Tareas del checklist de una intervención
-- -----------------------------------------------------------------
CREATE TABLE tareas (
    id              BIGSERIAL PRIMARY KEY,
    intervencion_id BIGINT NOT NULL REFERENCES intervenciones (id) ON DELETE CASCADE,
    descripcion     VARCHAR(255) NOT NULL,
    orden           INT         NOT NULL DEFAULT 0,
    completada      BOOLEAN     NOT NULL DEFAULT FALSE,
    nota            VARCHAR(500),
    created_at      TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT uq_tareas_intervencion_orden UNIQUE (intervencion_id, orden)
);

-- -----------------------------------------------------------------
-- Checklist de software de una intervención
-- -----------------------------------------------------------------
CREATE TABLE intervencion_software (
    id                BIGSERIAL PRIMARY KEY,
    intervencion_id   BIGINT NOT NULL REFERENCES intervenciones (id) ON DELETE CASCADE,
    software_id       BIGINT NOT NULL REFERENCES software (id) ON DELETE RESTRICT,
    estado            VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    version_instalada VARCHAR(50),
    nota              VARCHAR(500),
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_ivs_intervencion_software UNIQUE (intervencion_id, software_id),
    CONSTRAINT chk_ivs_estado CHECK (estado IN ('PENDIENTE', 'COMPROBADO', 'INSTALADO', 'ACTUALIZADO', 'NO_APLICA'))
);
