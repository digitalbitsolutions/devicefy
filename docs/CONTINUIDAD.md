# Continuidad de desarrollo

Documento de traspaso para retomar Devicefy sin reconstruir el contexto de la última iteración.

## Punto de partida

- Rama: `main`.
- Commit funcional: `fa79c5c` (`feat: improve project and equipment administration`).
- Fecha de validación: 9 de agosto de 2026.
- PostgreSQL local validado con las migraciones Flyway `V1`–`V10`.
- Verificaciones ejecutadas correctamente:
  - Desde `backend`: `.\mvnw.cmd test`, 1 prueba y 0 fallos.
  - Desde `frontend`: `npm run build`, correcto con aviso de bundle superior a 500 kB.
  - Desde `frontend`: `npm run lint`, correcto.

## Qué se entregó en la última iteración

- Filtros de equipos adaptados a Admin y Técnico.
- Paginación y total de registros en los listados de equipos.
- Proyecto único Tarragona, absorbiendo Terres de l'Ebre.
- Seis centros normalizados y asociados a Cataluña/Tarragona.
- Comunidad autónoma y responsables en la gestión de centros.
- Creación y edición de proyectos con comunidad, provincia, estado, múltiples centros y múltiples técnicos.
- Botón `OK` en todos los selectores múltiples de proyectos y asignaciones de usuarios.
- API y modelo de datos para persistir las relaciones proyecto-centro y proyecto-técnico.

## Datos de negocio que no deben alterarse accidentalmente

### Proyecto

Solo debe existir **Tarragona**. El total de equipos puede variar con las importaciones y el trabajo de campo; no debe codificarse en frontend.

### Centros

1. CAP Baix Ebre
2. CAP Salou
3. CAP Sant Pere
4. CAP Torreforta
5. Hospital Joan XXIII
6. Hospital Verge de la Cinta

Todos pertenecen a comunidad autónoma **Cataluña** y provincia **Tarragona**.

### Migraciones recientes

| Migración | Finalidad |
|---|---|
| `V6__proyectos_centros.sql` | Relación múltiple entre proyectos y centros |
| `V7__consolidar_proyecto_tarragona.sql` | Unificación de proyectos en Tarragona |
| `V8__comunidad_cataluna_centros.sql` | Comunidad autónoma de centros |
| `V9__consolidar_centros_tarragona.sql` | Normalización y depuración a seis centros |
| `V10__comunidad_proyecto_tarragona.sql` | Comunidad autónoma del proyecto Tarragona |

Estas migraciones ya se aplicaron en la base local. Crear `V11` o posterior para cualquier cambio de esquema o datos.

## Archivos clave

| Área | Archivo principal |
|---|---|
| Proyectos y multiselección | `frontend/src/pages/DesplieguesPage.tsx` |
| Equipos, filtros y paginación | `frontend/src/pages/EquiposPage.tsx` |
| Equipos de un proyecto | `frontend/src/pages/DespliegueDetallePage.tsx` |
| Centros y responsables | `frontend/src/pages/CentrosPage.tsx` |
| Asignaciones de usuarios | `frontend/src/pages/UsuariosPage.tsx` |
| Cliente y tipos API | `frontend/src/lib/api.ts` |
| API de proyectos/importación | `backend/src/main/java/com/devicefy/backend/controller/ImportacionController.java` |
| Lógica de proyectos/importación | `backend/src/main/java/com/devicefy/backend/service/impl/ImportacionServiceImpl.java` |
| Alcance y filtros de equipos | `backend/src/main/java/com/devicefy/backend/service/impl/EquipoServiceImpl.java` |
| Trabajo del técnico | `backend/src/main/java/com/devicefy/backend/controller/TrabajoController.java` |

## Cómo retomar

```powershell
git pull --ff-only origin main
docker compose up -d

cd backend
.\mvnw.cmd spring-boot:run
```

En otra terminal:

```powershell
cd frontend
npm install
npm run dev
```

Comprobar primero con Admin:

1. `/despliegues`: Tarragona aparece una sola vez.
2. Crear/editar proyecto: los multiselectores de centros y técnicos muestran `OK` y se cierran al pulsarlo.
3. `/centros`: solo aparecen los seis centros válidos con Cataluña/Tarragona.
4. `/equipos`: filtros, paginación y total de registros funcionan conjuntamente.

Después iniciar sesión como Técnico y comprobar los filtros de proyecto/provincia y que solo accede a los centros asignados.

## Siguiente trabajo recomendado

Comenzar por el flujo de intervenciones:

1. Definir el contrato de una intervención y sus transiciones.
2. Separar el historial inmutable de la actualización de la ficha actual del equipo.
3. Implementar servicio y pruebas backend.
4. Añadir la ficha/historial y el checklist al frontend.

No comenzar Informes ni Configuración antes de cerrar ese contrato, porque ambas pantallas dependerán de los estados y datos definitivos de las intervenciones.

## Riesgos y deuda conocida

- La cobertura automatizada del backend es mínima; falta probar reglas y permisos.
- No hay pruebas automatizadas de frontend.
- El bundle principal supera 500 kB y necesita división por rutas.
- Swagger está habilitado por defecto.
- Las credenciales predeterminadas solo son apropiadas para desarrollo local.
- Las migraciones de consolidación modifican datos existentes; probar las futuras migraciones sobre una copia antes de producción.
