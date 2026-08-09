UPDATE despliegues
   SET comunidad_autonoma = 'Cataluña'
 WHERE provincia = 'Tarragona'
   AND (comunidad_autonoma IS NULL OR trim(comunidad_autonoma) = '');
