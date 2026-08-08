-- =============================================================
-- Devicefy - V2: Datos iniciales
-- Roles, catálogo de estados (configurables) y catálogo de software
-- =============================================================

-- -----------------------------------------------------------------
-- Roles de la aplicación
-- -----------------------------------------------------------------
INSERT INTO roles (nombre) VALUES
    ('ADMIN'),
    ('TECNICO'),
    ('CONSULTA');

-- -----------------------------------------------------------------
-- Estados de equipo (configurables).
-- P / I / U se definen aquí sin cambios de esquema.
-- -----------------------------------------------------------------
INSERT INTO estados_equipo (codigo, nombre, descripcion, orden) VALUES
    ('PENDIENTE',   'Pendiente',       'Equipo localizado pendiente de intervención', 10),
    ('EN_PROCESO',  'En proceso',      'Equipo con una intervención abierta',          20),
    ('FINALIZADO',  'Finalizado',      'Equipo intervenido y actualizado',             30),
    ('BAJA',        'De baja',         'Equipo retirado del inventario activo',        40);

-- -----------------------------------------------------------------
-- Catálogo inicial de software a comprobar / instalar
-- -----------------------------------------------------------------
INSERT INTO software (nombre, fabricante, version_referencia, categoria) VALUES
    ('Sistema operativo',      'Microsoft',    'Windows 11', 'Sistema'),
    ('Suite ofimática',        'Microsoft',    'Office 2021', 'Ofimática'),
    ('Antivirus corporativo',  NULL,           NULL,          'Seguridad'),
    ('ERP de gestión',         NULL,           NULL,          'Gestión'),
    ('Cliente de correo',      NULL,           NULL,          'Comunicación'),
    ('Navegador web',          NULL,           NULL,          'Comunicación');
