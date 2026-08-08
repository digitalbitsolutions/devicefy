# Devicefy — Inventario de equipos e intervenciones

Aplicación para centralizar equipos, periféricos, red, checklist e historial de intervenciones, sustituyendo el trabajo manual con Excel. Documento de diseño: `../plan_crm_inventario_spring_boot.md`.

## Estado del proyecto

Implementadas las **fases 2 y 3** del plan:

- Modelo de datos relacional diseñado y versionado con **Flyway**.
- Esqueleto **Spring Boot 4.1.0** (Java 21) conectado a **PostgreSQL** vía Docker.
- Entidades JPA, repositorios y validación de esquema en el arranque.
- Seguridad mínima (placeholder): autenticación JWT llega en la fase 4.
- Esqueleto **frontend** React + TypeScript + Vite + Material UI (pantallas en fase 8).

## Estructura

```
web/
├── backend/          Spring Boot (Java 21, Maven)
│   ├── src/main/java/com/devicefy/backend
│   │   ├── config/       SecurityConfig (placeholder), DataInitializer (admin)
│   │   ├── domain/       Entidades JPA y enums
│   │   └── repository/   Repositorios Spring Data
│   └── src/main/resources/db/migration/   V1__schema.sql, V2__seed.sql
├── frontend/         React + TS + Vite + MUI (esqueleto)
│   └── src/          api client (axios), providers (React Query, Router)
└── docker-compose.yml  PostgreSQL 17 para desarrollo
```

## Requisitos

- JDK 21 (Temurin). `JAVA_HOME` apuntando a él.
- Node.js 20+ y npm.
- Docker Desktop en ejecución.
- Maven no es necesario: el backend usa el **Maven Wrapper** (`mvnw`).

## Arranque

### 1. Base de datos

```powershell
cd web
docker compose up -d          # PostgreSQL en localhost:5432 (db devicefy / user devicefy / pass devicefy)
```

### 2. Backend

```powershell
cd web/backend
.\mvnw.cmd spring-boot:run
```

En el arranque Flyway aplica las migraciones y se crea el usuario administrador si no existe.

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Credenciales iniciales (`admin` / `admin123`). Cambiar vía variables de entorno `ADMIN_USERNAME` y `ADMIN_PASSWORD`, y en producción modificar también `JWT_SECRET`.

### 3. Frontend

```powershell
cd web/frontend
npm install
npm run dev            # http://localhost:5173 (proxy /api -> localhost:8080)
```

## Configuración por variables de entorno

| Variable               | Valor por defecto | Uso                          |
|------------------------|-------------------|------------------------------|
| `DB_HOST` / `DB_PORT`  | `localhost` / `5432` | Conexión PostgreSQL        |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `devicefy` | Credenciales BD |
| `SERVER_PORT`          | `8080`            | Puerto de la API             |
| `JWT_SECRET`           | valor de ejemplo  | Secreto JWT (fase 4)         |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / `admin123` | Usuario inicial |

## Modelo de datos

Principio de historial: cada actuación genera una **intervención** independiente y de solo lectura; la ficha actual del equipo se actualiza por separado.

| Tabla                     | Responsabilidad                                             |
|---------------------------|-------------------------------------------------------------|
| `usuarios` / `roles`      | Técnicos de la aplicación y permisos (ADMIN, TECNICO, CONSULTA) |
| `centros` / `ubicaciones` | CAP, hospital o edificio y planta/zona/despacho              |
| `usuarios_asignados`      | Persona o puesto que utiliza el equipo                       |
| `estados_equipo`          | **Catálogo configurable de estados** (P/I/U se definen aquí, sin cambios de esquema) |
| `equipos`                 | CPU/estación identificada por hostname, número de serie y etiqueta patrimonial (únicos) |
| `redes`                   | IP, máscara, puerta de enlace, DNS, tipo de asignación (1:1) |
| `perifericos`             | Monitores, impresoras, teclados, etc. (con o sin equipo)     |
| `software`                | Catálogo de aplicaciones a comprobar/instalar                |
| `intervenciones`          | Historial inmutable: técnico, tipo, estado, fechas, observaciones, incidencias |
| `tareas`                  | Checklist de la intervención (descripción, orden, completada, nota) |
| `intervencion_software`   | Checklist de software por intervención (PENDIENTE, COMPROBADO, INSTALADO, ACTUALIZADO, NO_APLICA) |

Reglas clave:

- Unicidad de equipos por `hostname`, `numero_serie` y `etiqueta_patrimonial` (los `NULL` se permiten y no colisionan).
- `equipos.estado` referencia `estados_equipo.codigo` (configurable; seeded: PENDIENTE, EN_PROCESO, FINALIZADO, BAJA).
- `redes.ip` única y `redes.equipo_id` 1:1.
- Las intervenciones se borran en cascada con sus tareas y checks de software, pero nunca se elimina el historial de un equipo finalizado.
- `ddl-auto: validate`: si las entidades y el esquema divergen, la aplicación no arranca (las migraciones Flyway son la fuente de verdad).

## Pruebas

```powershell
cd web/backend
.\mvnw.cmd test
```

## Próximos pasos (plan)

1. **Fase 4**: seguridad JWT (login, roles, endpoints protegidos).
2. **Fase 5**: CRUD de centros, ubicaciones, equipos y periféricos.
3. **Fase 6**: importación de Excel con Apache POI e informe de duplicados.
4. **Fase 7**: flujo de intervenciones (formulario, estados, tareas, software).
5. **Fase 8**: pantallas React conectadas a la API.
