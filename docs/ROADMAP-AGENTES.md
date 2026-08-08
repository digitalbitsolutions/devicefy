# Roadmap de desarrollo orquestado con agentes locales (Ollama)

**Proyecto:** Devicefy (CRM de inventario de equipos e intervenciones).
**Estrategia:** todo el desarrollo se orquesta desde **opencode**, delegando tareas a **subagentes** que ejecutan **modelos locales de Ollama** (gratis, sin internet, sin consumir APIs de pago).

## Modelos locales disponibles

| Modelo | Perfil de uso | Tareas recomendadas |
|---|---|---|
| `deepseek-v3.1` | Razonamiento y código fuerte | Lógica backend, servicios, seguridad, importación Excel |
| `deepseek-coder` | Código puro, ediciones rápidas | Refactor, consultas JPA, scripts SQL |
| `qwen2.5` | Equilibrado, buen frontend | React/TS, componentes MUI, formularios |
| `gemma3` | Ligero | Tareas auxiliares, resúmenes, validaciones |
| `llama3.1` / `llama3.2` | Fallback | Tareas sencillas de respaldo |
| `mistral` | Alternativa general | Fallback cuando otro modelo falla |

**Regla de orquestación:** tarea compleja → modelo más capaz (deepseek-v3.1); tarea repetitiva o de frontend → qwen2.5; tareas triviales → gemma3. Siempre verificando el resultado (`mvnw test`, `npm run build`) desde opencode.

## Arquitectura de orquestación

```
opencode (orquestador)
  ├── planifica y divide el trabajo en tareas
  ├── delega cada tarea a un subagente configurado con modelo local Ollama
  │     ├── subagente "backend-dev"   -> ollama/deepseek-v3.1
  │     ├── subagente "frontend-dev"  -> ollama/qwen2.5
  │     ├── subagente "explore"       -> modelo local ligero
  │     └── ...
  ├── recibe resultados parciales y los integra
  └── ejecuta las verificaciones (build, tests, docker) y corrige
```

- Ollama se expone en `http://localhost:11434` (API compatible con OpenAI en `/v1`).
- Los subagentes usan únicamente modelos locales; ningún secreto sale de la máquina.

## Fases y tareas delegadas

### Fase 4 — Seguridad JWT
- [ ] `backend-dev` (deepseek-v3.1): implementar login, emisión/validación de JWT y `UserDetailsService`.
- [ ] `backend-dev`: proteger endpoints por rol (ADMIN / TECNICO / CONSULTA) en `SecurityConfig`.
- [ ] `backend-dev`: refresh token y logout (opcional).
- [ ] `explore`: revisar que ningún endpoint queda sin proteger.

### Fase 5 — Inventario (CRUD)
- [ ] `backend-dev`: controladores y DTOs de `Centro`, `Ubicacion`, `Equipo`, `Periferico`.
- [ ] `backend-dev`: búsqueda y filtros (hostname, serie, etiqueta, estado, centro).
- [ ] `frontend-dev` (qwen2.5): listados con buscador y filtros, formularios MUI + React Hook Form.
- [ ] `backend-dev`: validación Bean Validation y manejo de duplicados.

### Fase 6 — Importación de Excel
- [ ] `backend-dev` (deepseek-v3.1): parser con Apache POI (plantilla IDI y CAPs Tarragona).
- [ ] `backend-dev`: informe de errores/duplicados por hostname, serie y etiqueta patrimonial.
- [ ] `frontend-dev`: pantalla de subida de archivo y vista del informe.
- [ ] `explore`: validar normalización de fechas a `LocalDate` y casos de IP repetida.

### Fase 7 — Intervenciones
- [ ] `backend-dev`: formulario de intervención, transición de estados y cierre con `fecha_fin`.
- [ ] `backend-dev`: checklist de tareas y de software por intervención.
- [ ] `frontend-dev`: ficha de equipo, historial y checklist interactivos.
- [ ] `explore`: verificar regla de inmutabilidad del historial finalizado.

### Fase 8 — Frontend completo
- [ ] `frontend-dev`: login, dashboard con KPIs, listados, fichas e historial.
- [ ] `frontend-dev`: React Query + Axios conectados a la API con JWT.
- [ ] `backend-dev`: CORS definitivo y ajustes de DTO según consumo real.

### Fase 9 — Informes y exportación
- [ ] `backend-dev`: dashboard (pendientes / en proceso / finalizados) y endpoints de agregación.
- [ ] `backend-dev`: exportación de fichas e informes (Excel/PDF).
- [ ] `frontend-dev`: gráficas y botones de exportación.

### Fase 10 — Calidad y despliegue
- [ ] `backend-dev`: tests (JUnit + Testcontainers) y limpieza de código.
- [ ] `backend-dev`: Dockerfile y docker-compose de producción (app + BD).
- [ ] `explore`: revisión de seguridad y copias de seguridad.
- [ ] `frontend-dev`: build de producción y comprobación responsive.

## Criterios de aceptación por tarea
1. `.\mvnw.cmd test` en verde (backend).
2. `npm run build` sin errores (frontend).
3. Arranque completo con `docker compose up -d` y Flyway aplicado.
4. Verificación manual de los endpoints en Swagger UI.

## Completado
- [x] Ollama 0.24.0 verificado y respondiendo en `http://localhost:11434` (API y `/v1`).
- [x] Registrados en `~/.config/opencode/opencode.jsonc`: proveedor `ollama` con todos los modelos locales y los subagentes `backend-dev` (deepseek-coder:6.7b) y `frontend-dev` (qwen2.5:7b).
- [x] Prueba de extremo a extremo: `deepseek-coder:6.7b` generó `CentroService.java`, se revisó, integró y `mvnw compile` pasó.

> **Nota:** tras cambiar `opencode.jsonc`, hay que reiniciar opencode para que cargue la config. Los subagentes `backend-dev` y `frontend-dev` quedarán disponibles en el selector de agentes.
