-- Conserva únicamente los seis centros reales del proyecto Tarragona.
-- Unifica el duplicado de Hospital Verge de la Cinta sin perder relaciones.
DO $$
DECLARE
    verge_destino_id BIGINT;
    verge_origen_id BIGINT;
BEGIN
    SELECT id INTO verge_destino_id
      FROM centros
     WHERE upper(trim(nombre)) = 'HOSPITAL VERGE DE LA CINTA'
     ORDER BY id
     LIMIT 1;

    SELECT id INTO verge_origen_id
      FROM centros
     WHERE upper(trim(nombre)) = 'H VERGE DE LA CINTA'
     ORDER BY id
     LIMIT 1;

    IF verge_destino_id IS NOT NULL AND verge_origen_id IS NOT NULL THEN
        INSERT INTO usuario_centros (usuario_id, centro_id)
        SELECT usuario_id, verge_destino_id
          FROM usuario_centros
         WHERE centro_id = verge_origen_id
        ON CONFLICT (usuario_id, centro_id) DO NOTHING;

        INSERT INTO despliegue_centros (despliegue_id, centro_id, created_at)
        SELECT despliegue_id, verge_destino_id, created_at
          FROM despliegue_centros
         WHERE centro_id = verge_origen_id
        ON CONFLICT (despliegue_id, centro_id) DO NOTHING;

        DELETE FROM usuario_centros WHERE centro_id = verge_origen_id;
        DELETE FROM despliegue_centros WHERE centro_id = verge_origen_id;

        UPDATE centro_responsables
           SET centro_id = verge_destino_id
         WHERE centro_id = verge_origen_id;

        -- Reutiliza una ubicación equivalente si existiera en ambos centros.
        UPDATE equipos equipo
           SET ubicacion_id = ubicacion_destino.id
          FROM ubicaciones ubicacion_origen
          JOIN ubicaciones ubicacion_destino
            ON ubicacion_destino.centro_id = verge_destino_id
           AND lower(trim(ubicacion_destino.nombre)) = lower(trim(ubicacion_origen.nombre))
         WHERE ubicacion_origen.centro_id = verge_origen_id
           AND equipo.ubicacion_id = ubicacion_origen.id;

        DELETE FROM ubicaciones ubicacion_origen
         USING ubicaciones ubicacion_destino
         WHERE ubicacion_origen.centro_id = verge_origen_id
           AND ubicacion_destino.centro_id = verge_destino_id
           AND lower(trim(ubicacion_destino.nombre)) = lower(trim(ubicacion_origen.nombre));

        UPDATE ubicaciones
           SET centro_id = verge_destino_id
         WHERE centro_id = verge_origen_id;

        UPDATE equipos
           SET centro_id = verge_destino_id
         WHERE centro_id = verge_origen_id;

        DELETE FROM centros WHERE id = verge_origen_id;
    END IF;

    -- Los cinco equipos generados en pruebas se conservan en inventario, pero
    -- se desvinculan de los falsos centros Planta 1/2/3.
    UPDATE equipos
       SET ubicacion_id = NULL
     WHERE ubicacion_id IN (
         SELECT ubicacion.id
           FROM ubicaciones ubicacion
           JOIN centros centro ON centro.id = ubicacion.centro_id
          WHERE centro.nombre IN ('Planta 1', 'Planta 2', 'Planta 3', 'Centre BCN 902')
     );

    UPDATE equipos
       SET centro_id = NULL
     WHERE centro_id IN (
         SELECT id
           FROM centros
          WHERE nombre IN ('Planta 1', 'Planta 2', 'Planta 3', 'Centre BCN 902')
     );

    DELETE FROM ubicaciones
     WHERE centro_id IN (
         SELECT id
           FROM centros
          WHERE nombre IN ('Planta 1', 'Planta 2', 'Planta 3', 'Centre BCN 902')
     );

    DELETE FROM centros
     WHERE nombre IN ('Planta 1', 'Planta 2', 'Planta 3', 'Centre BCN 902');

    UPDATE centros
       SET nombre = CASE upper(trim(nombre))
           WHEN 'CAP BAIX EBRE' THEN 'CAP Baix Ebre'
           WHEN 'CAP SALOU' THEN 'CAP Salou'
           WHEN 'REUS - CAP SANT PERE' THEN 'CAP Sant Pere'
           WHEN 'CAP TORREFORTA - LA GRANJA' THEN 'CAP Torreforta'
           WHEN 'HU JOAN XXIII' THEN 'Hospital Joan XXIII'
           WHEN 'HOSPITAL VERGE DE LA CINTA' THEN 'Hospital Verge de la Cinta'
           ELSE nombre
       END,
           comunidad_autonoma = 'Cataluña',
           provincia = 'Tarragona'
     WHERE upper(trim(nombre)) IN (
         'CAP BAIX EBRE',
         'CAP SALOU',
         'REUS - CAP SANT PERE',
         'CAP TORREFORTA - LA GRANJA',
         'HU JOAN XXIII',
         'HOSPITAL VERGE DE LA CINTA'
     );
END $$;
