# Devicefy — Inventario y despliegues de equipos

Aplicación web para administrar centros, equipos, periféricos, proyectos de despliegue y el trabajo de los técnicos. Sustituye el seguimiento manual mediante hojas de cálculo y mantiene la información operativa en PostgreSQL.

## Estado actual

El proyecto dispone de una base funcional de administración y operación:

- Autenticación JWT y permisos por rol (`ADMIN`, `TECNICO`, `CONSULTA`).
- Dashboard administrativo con indicadores y gráficas.
- CRUD de centros, ubicaciones, equipos, periféricos y usuarios.
- Importación de inventario Excel con Apache POI, detección de formatos tabular/IDI y mapeo opcional de cabeceras con Ollama local.
- Proyectos con centros y técnicos asignables mediante selección múltiple.
- Listados de equipos con filtros por centro, estado, proyecto, provincia y técnico según el rol, además de paginación y total de registros.
- Flujo básico del técnico para consultar y procesar los equipos de sus centros.
- Gestión de responsables de cada centro.

El corte documentado corresponde al commit funcional `fa79c5c` del 9 de agosto de 2026. La guía para retomar el trabajo está en [docs/CONTINUIDAD.md](docs/CONTINUIDAD.md).

## Reglas de negocio vigentes

- Actualmente solo existe el proyecto **Tarragona**. Terres de l'Ebre forma parte de Tarragona y no se mantiene como proyecto separado.
- Todos los centros actuales pertenecen a **Cataluña**, provincia de **Tarragona**.
- Los únicos centros válidos en el conjunto de datos actual son:
  - CAP Baix Ebre
  - CAP Salou
  - CAP Sant Pere
  - CAP Torreforta
  - Hospital Joan XXIII
  - Hospital Verge de la Cinta
- Las migraciones Flyway `V7`–`V10` consolidan esos datos. No se deben modificar migraciones ya aplicadas; cualquier cambio posterior debe añadirse en una migración nueva.
- El administrador puede crear y editar proyectos, asignando múltiples centros y técnicos. Los selectores múltiples se confirman con el botón `OK`; el formulario se persiste con `Crear` o `Guardar`.

## Tecnologías

| Capa | Tecnologías principales |
|---|---|
| Backend | Java 21, Spring Boot 4.1, Spring Security, JPA/Hibernate, Flyway, Apache POI |
| Base de datos | PostgreSQL 17 |
| Frontend | React 19, TypeScript 6, Vite 8, Material UI 9, React Query |
| Desarrollo local | Docker Compose, Maven Wrapper, npm |

## Estructura

```text
web/
├── backend/                  API Spring Boot
│   └── src/main/
│       ├── java/...          controladores, dominio, DTO, repositorios y servicios
│       └── resources/
│           └── db/migration/ migraciones Flyway V1–V10
├── frontend/                 cliente React/Vite
│   └── src/                  páginas, componentes y cliente API
├── docs/                     roadmap, continuidad y especificación de importación
└── docker-compose.yml        PostgreSQL para desarrollo
```

## Requisitos

- JDK 21 con `JAVA_HOME` configurado.
- Node.js 20 o posterior y npm.
- Docker Desktop.
- Ollama es opcional. Si está desactivado o no responde, la importación conserva el mapeo determinista de columnas conocidas.

## Arranque local

Desde la raíz `web`:

### 1. PostgreSQL

```powershell
docker compose up -d
docker compose ps
```

La configuración local predeterminada es base `devicefy`, usuario `devicefy`, contraseña `devicefy`, puerto `5432`.

### 2. Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI: <http://localhost:8080/v3/api-docs>

### 3. Frontend

En otra terminal:

```powershell
cd frontend
npm install
npm run dev
```

Frontend: <http://localhost:5173>. Vite redirige `/api` al backend local.

El usuario inicial es `admin` / `admin123`. Debe cambiarse mediante variables de entorno fuera del entorno local.

## Variables de entorno

| Variable | Predeterminado | Uso |
|---|---|---|
| `DB_HOST` / `DB_PORT` | `localhost` / `5432` | Servidor PostgreSQL |
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `devicefy` | Base de datos y credenciales |
| `SERVER_PORT` | `8080` | Puerto de la API |
| `JWT_SECRET` | secreto local de ejemplo | Firma JWT; obligatorio cambiarlo en producción |
| `JWT_EXPIRATION_MS` | `86400000` | Duración del token |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | `admin` / `admin123` | Administrador inicial |
| `OLLAMA_ENABLED` | `true` | Activa el apoyo local para reconocer columnas |
| `OLLAMA_URL` | `http://localhost:11434` | Endpoint de Ollama |
| `OLLAMA_MODEL` | `qwen2.5:7b` | Modelo local |
| `OLLAMA_TIMEOUT_MS` | `30000` | Tiempo máximo de respuesta |

## Verificación

```powershell
cd backend
.\mvnw.cmd test

cd ..\frontend
npm run build
npm run lint
```

En el corte actual pasan las pruebas del backend, la compilación TypeScript/Vite y Oxlint. Vite avisa que el paquete principal supera 500 kB; es una mejora pendiente, no un error de compilación.

## Documentación

- [Continuidad de desarrollo](docs/CONTINUIDAD.md)
- [Roadmap](docs/ROADMAP-AGENTES.md)
- [Especificación de importación Excel](docs/FASE-6-IMPORTACION.md)
- [Frontend](frontend/README.md)
