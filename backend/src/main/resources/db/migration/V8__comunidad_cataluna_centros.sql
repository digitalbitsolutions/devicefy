-- Los centros actuales pertenecen a Cataluña. Se completa únicamente el dato
-- ausente para no sobrescribir información explícita en futuras instalaciones.
UPDATE centros
   SET comunidad_autonoma = 'Cataluña'
 WHERE comunidad_autonoma IS NULL
    OR trim(comunidad_autonoma) = '';
