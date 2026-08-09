# Roadmap de Devicefy

Última actualización: **9 de agosto de 2026**. Este documento refleja el estado real del repositorio después del commit funcional `fa79c5c`.

## Completado

### Base, seguridad e inventario

- [x] Modelo PostgreSQL versionado mediante Flyway (`V1`–`V10`).
- [x] Autenticación JWT y rutas protegidas.
- [x] Roles `ADMIN`, `TECNICO` y `CONSULTA`.
- [x] CRUD de centros, responsables, ubicaciones, equipos, periféricos y usuarios.
- [x] Asignación de centros y proyectos a técnicos.
- [x] Dashboard administrativo con indicadores y gráficas.

### Importación y proyectos

- [x] Importación Excel tabular e IDI con Apache POI.
- [x] Detección de duplicados y actualización del inventario.
- [x] Reconocimiento opcional de cabeceras mediante Ollama local con fallback determinista.
- [x] Administración de proyectos: crear, editar, eliminar y consultar equipos.
- [x] Selección múltiple de centros y técnicos en creación/edición, con confirmación `OK`.
- [x] Consolidación de Tarragona como único proyecto actual.
- [x] Consolidación de los seis centros válidos de Tarragona y asignación de Cataluña/Tarragona.

### Listados y alcance por rol

- [x] Filtros administrativos de equipos, incluido técnico.
- [x] Filtros para técnicos por proyecto y provincia, además de los existentes.
- [x] Paginación y total de registros en listados de equipos.
- [x] Consulta del trabajo del técnico limitada a sus centros.
- [x] Procesamiento básico de un equipo: estado, hostname nuevo, usuario, red y observaciones.

## Próxima iteración recomendada

### 1. Completar el flujo de intervenciones

- [ ] Crear una intervención inmutable por cada actuación, en lugar de depender únicamente de `despliegue_equipos`.
- [ ] Implementar checklist de tareas y software.
- [ ] Registrar inicio, pausa, finalización, incidencias y técnico responsable.
- [ ] Añadir historial completo a la ficha del equipo.
- [ ] Definir y probar las transiciones de estado permitidas.

### 2. Informes y exportación

- [ ] Sustituir la pantalla provisional de Informes.
- [ ] Exportar inventario y avance por proyecto/centro/técnico a Excel.
- [ ] Añadir informe de pendientes, en proceso, finalizados e incidencias.
- [ ] Evaluar exportación PDF solo después de cerrar los formatos de negocio.

### 3. Configuración

- [ ] Sustituir la pantalla provisional de Configuración.
- [ ] Administrar catálogos de estados, tipos de periférico y software.
- [ ] Definir qué opciones son globales y cuáles pertenecen a un proyecto.

### 4. Calidad y producción

- [ ] Ampliar la cobertura: actualmente solo existe una prueba de arranque de contexto.
- [ ] Añadir pruebas de servicios, permisos por rol, filtros y migraciones con una base aislada.
- [ ] Incorporar pruebas de frontend para formularios y selectores múltiples.
- [ ] Dividir el bundle principal del frontend; Vite informa un chunk superior a 500 kB.
- [ ] Crear Dockerfiles y un `docker-compose` de producción para frontend, backend y PostgreSQL.
- [ ] Desactivar Swagger o protegerlo en producción.
- [ ] Configurar secretos, copias de seguridad y restauración.
- [ ] Revisar accesibilidad y comportamiento responsive.

## Decisiones que deben conservarse

1. **Proyecto:** Tarragona es el único proyecto vigente; Terres de l'Ebre no es independiente.
2. **Centros:** solo deben existir los seis centros enumerados en el README, salvo una decisión explícita posterior del negocio.
3. **Geografía:** los centros actuales son Cataluña / Tarragona.
4. **Migraciones:** nunca editar una migración aplicada. Toda corrección se añade con el siguiente número Flyway.
5. **Asignaciones:** proyectos y usuarios admiten relaciones múltiples con centros/técnicos.
6. **Selectores múltiples:** `OK` confirma visualmente y cierra el desplegable; `Crear`/`Guardar` persiste el formulario completo.

## Criterios de aceptación

Antes de cerrar una iteración:

1. Desde `backend`, `.\mvnw.cmd test` finaliza en verde.
2. Desde `frontend`, `npm run build` finaliza sin errores.
3. Desde `frontend`, `npm run lint` finaliza sin errores.
4. Flyway valida y arranca con PostgreSQL local.
5. Se comprueban manualmente los flujos afectados con roles Admin y Técnico.
6. README, roadmap y continuidad se actualizan si cambian reglas o prioridades.
