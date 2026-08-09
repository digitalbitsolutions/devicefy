-- Consolida Terres de l'Ebre dentro del proyecto Tarragona.
-- Los equipos permanecen en inventario y conservan su avance y técnico.
DO $$
DECLARE
    tarragona_id BIGINT;
    origen_id BIGINT;
BEGIN
    SELECT id
      INTO tarragona_id
      FROM despliegues
     WHERE lower(trim(nombre)) = 'tarragona'
     ORDER BY id
     LIMIT 1;

    IF tarragona_id IS NULL THEN
        RAISE EXCEPTION 'No existe el proyecto Tarragona';
    END IF;

    FOR origen_id IN
        SELECT id
          FROM despliegues
         WHERE id <> tarragona_id
           AND lower(trim(nombre)) IN (
               'terres de l''ebre',
               'terres del ebre',
               'terras del ebro'
           )
    LOOP
        INSERT INTO despliegue_tecnicos (despliegue_id, usuario_id, created_at)
        SELECT tarragona_id, usuario_id, created_at
          FROM despliegue_tecnicos
         WHERE despliegue_id = origen_id
        ON CONFLICT (despliegue_id, usuario_id) DO NOTHING;

        INSERT INTO despliegue_centros (despliegue_id, centro_id, created_at)
        SELECT tarragona_id, centro_id, created_at
          FROM despliegue_centros
         WHERE despliegue_id = origen_id
        ON CONFLICT (despliegue_id, centro_id) DO NOTHING;

        -- Si un equipo ya estaba en ambos proyectos, conserva la relación de
        -- Tarragona y completa sus datos con la relación de Terres de l'Ebre.
        UPDATE despliegue_equipos destino
           SET hostname_actual = COALESCE(destino.hostname_actual, origen.hostname_actual),
               hostname_nuevo = COALESCE(destino.hostname_nuevo, origen.hostname_nuevo),
               estado_renove = COALESCE(destino.estado_renove, origen.estado_renove),
               anio_renove = COALESCE(destino.anio_renove, origen.anio_renove),
               perfil_imagen = COALESCE(destino.perfil_imagen, origen.perfil_imagen),
               tecnico_id = COALESCE(destino.tecnico_id, origen.tecnico_id),
               fecha_toma = COALESCE(destino.fecha_toma, origen.fecha_toma),
               estado = CASE
                   WHEN destino.estado = 'HECHO' OR origen.estado = 'HECHO' THEN 'HECHO'
                   WHEN destino.estado = 'EN_PROCESO' OR origen.estado = 'EN_PROCESO' THEN 'EN_PROCESO'
                   WHEN destino.estado = 'CANCELADO' OR origen.estado = 'CANCELADO' THEN 'CANCELADO'
                   ELSE 'PENDIENTE'
               END
          FROM despliegue_equipos origen
         WHERE destino.despliegue_id = tarragona_id
           AND origen.despliegue_id = origen_id
           AND destino.equipo_id = origen.equipo_id;

        DELETE FROM despliegue_equipos origen
         USING despliegue_equipos destino
         WHERE origen.despliegue_id = origen_id
           AND destino.despliegue_id = tarragona_id
           AND origen.equipo_id = destino.equipo_id;

        UPDATE despliegue_equipos
           SET despliegue_id = tarragona_id
         WHERE despliegue_id = origen_id;

        DELETE FROM despliegues WHERE id = origen_id;
    END LOOP;

    UPDATE despliegues
       SET provincia = 'Tarragona'
     WHERE id = tarragona_id;

    -- Importaciones creadas únicamente para probar el mapeo de columnas.
    -- ON DELETE CASCADE retira su vínculo, pero no elimina los equipos.
    DELETE FROM despliegues
     WHERE nombre IN ('Prueba IA Ollama', 'Prueba IA Ollama 2', 'Prueba IA v2');
END $$;
