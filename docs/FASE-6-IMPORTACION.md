# Fase 6 — Importación de Excel (misiones de despliegue)

> **Estado actual (9 de agosto de 2026):** la importación está implementada. Aunque este documento conserva el análisis histórico de Girona/Tortosa, la regla vigente del producto es un único proyecto **Tarragona**; Terres de l'Ebre está integrado en él. Los centros válidos y las instrucciones de continuidad están en `README.md` y `docs/CONTINUIDAD.md`.

## 1. Contexto del negocio

- Devicefy gestiona **despliegues informáticos**: listas de equipos que los técnicos procesan
  físicamente en distintas sedes. Existen (al menos) tres despliegues con estructura similar:
  **Girona, Tarragona y Tortosa**.
- Cada despliegue es un fichero Excel; **cada fila = un equipo a procesar**.
- **Procesar = RENOVE** (término general, según el jefe): cubre tanto la **maquetación**
  (reimage / actualización Windows 10 a 11) como el **cambio de equipo**.
- El técnico recorre las instalaciones, localiza cada equipo por su **hostname actual**, lo
  comprueba contra el Excel y lo procesa.
- El **nuevo hostname** (si cambia) se decide en campo y NO está en el Excel → se registrará en
  la **Fase 7** (intervención).

### Etiquetas patrimoniales (dato de campo, no de importación)

- La etiqueta patrimonial se gestiona **durante la maqueta**: el técnico, al identificar y procesar
  el equipo, revisa si tiene pegatina de inventario.
- El equipo **puede no tener etiqueta**.
- Si **ya tiene etiqueta** (p. ej. códigos `GC…` de la Generalitat de Catalunya, que ya ha inventariado
  el equipo), el técnico la ingresa en la ficha sin problemas.
- Si recibe instrucción de **colocar una nueva pegatina**, o el equipo no tiene, el técnico la pone e
  ingresa el **nuevo código** en el sistema.
- Destino: `equipos.etiqueta_patrimonial` (columna ya existente, única). Se captura en la **Fase 7**
  (procesamiento / ficha del equipo).

## 2. Formato tabular ("Relació equips Tarragona_v2" y "CAPs Tarragona")

| Columna Excel | Contenido | Destino en Devicefy |
|---|---|---|
| `Nom` | Hostname actual + ID de inventario entre paréntesis (ej. `APSP192 (2571)`) | `equipos.hostname` (sin el paréntesis); ID → observaciones |
| `Entitat` | Jerarquía de entidad, ej. `Root entity > Informàtica` (sale de 2 tablas: ROOT e INFORMÀTICA) | tabla nueva `entidades` (conservar columna) |
| `Fabricant` | Fabricante (HP…) | `equipos.fabricante` |
| `Número de sèrie` | Nº de serie | `equipos.numero_serie` |
| `Model` | Modelo | `equipos.modelo` |
| `Sistema operatiu - Nom / Versió / Service pack` | SO, ej. Windows / 2009 / SP1 | `equipos.sistema_operativo` (ej. `Windows 2009 SP1`) |
| `Ubicació` | Ruta jerárquica `TARRAGONA > centro > edificio > planta > sala` | `centros` + `ubicaciones` (derivados de la ruta) |
| `Components - Processador` | CPU | `equipos.procesador` |
| `Suport - Incidències assignades` | Nº de incidencias | observaciones |
| `Estat` | Estado del sistema origen (`Installed`) | observaciones (estado origen) |
| `Xarxa - IP` | IPs (IPv4 + IPv6 link-local, varias por línea) | `redes.ip` (IPv4 principal); resto → observaciones |
| `Data de creació` | Fecha de alta en origen | observaciones |
| `(R/H/E)` | Estado del renove del equipo | `despliegue_equipos.estado_renove` (+ año) |
| `MAQUETA B2` | Perfil de imagen a aplicar (`SI`) | `despliegue_equipos.perfil_imagen` |

### Semántica de `(R/H/E)` (confirmado)

- `R` = RENOVE (planificado). **Siempre con año**: `R2026` = renove planificado en 2026 → **celda naranja**.
  El año se extrae del valor (`R2026` → `estado_renove=R`, `anio_renove=2026`) para identificar el proyecto de actualización.
- `E` = En proceso.
- `H` = Hecho → **celda amarilla**.

## 3. Formato IDI ("Plantilla equipos IDI")

Una hoja por equipo: etiquetas en columna B, valores en columna D.

- **Cabecera**: fecha de instalación, nombre de usuario, ubicación (zona y planta).
- **CPU**: hostname, serial number, marca, modelo, etiqueta LT2B, etiqueta GC.
- **MONITOR 1..3**: serie, marca, modelo, etiqueta LT2B, etiqueta GC → `perifericos` (tipo `MONITOR`).
- **IMPRESSORA**: serie, marca, modelo, IP, nombre → `perifericos` (tipo `IMPRESORA`) + `redes`.
- **OBSERVACIONS**.

## 4. Modelo de datos propuesto

### Confirmado sobre los datos reales del fichero "Relació…"

- `Entitat` tiene **un único valor**: `Root entity > Informàtica`. Es la jerarquía de entidades del
  sistema origen (posiblemente GLPI: la "Root entity" por defecto + la sub-entidad "Informàtica").
- `Estat` tiene un único valor (`Installed`) → se guarda como dato informativo.
- **No existe columna de etiqueta patrimonial** en el formato tabular; solo en la plantilla IDI (`LT2B`/`GC`).

### Nuevas tablas (migración Flyway V3)

- `despliegues`: `id`, `nombre` (Girona / Tarragona / Tortosa), `fichero_nombre`, `fecha_importacion`, `estado`.
- `entidades`: `id`, `nombre` (p. ej. `Informàtica`), `entidad_raiz` (p. ej. `Root entity`), `activo`.
- `despliegue_equipos`: `id`, `despliegue_id` FK, `equipo_id` FK, `hostname_actual`, `hostname_nuevo`
  (nullable, se rellena en Fase 7), `estado_renove` (`R`/`E`/`H`), `anio_renove` (nullable), `perfil_imagen`
  (p. ej. `B2`), `tecnico_id` FK `usuarios` (nullable), `fecha_toma` (nullable), `estado`.

  - `tecnico_id` + `fecha_toma`: registran **qué técnico procesa el equipo y cuándo lo tomó**
    (se rellena al "tomar" el equipo, durante la Fase 7).
  - `estado`: PENDIENTE / EN_PROCESO / HECHO / CANCELADO.

### Ajustes en tablas existentes

- `centros`: se crean a partir de `Ubicació` (segmento de centro, p. ej. `HU JOAN XXIII`, `CAP SANT PERE`).
  Se añade `centros.entidad_id` FK (vínculo centro → entidad) para conservar la relación.
- `ubicaciones`: se crean a partir del resto de la ruta (`Edifici D > Planta Baixa > Sala Densito`) →
  `nombre` = último segmento, `planta`/`zona` = segmentos intermedios.
- `equipos`: datos base (hostname, serie, fabricante, modelo, SO, procesador, centro, ubicación, observaciones).
- `redes`: IPv4 principal del equipo. `mascara`, `puerta_enlace`, `dns1`, `dns2` son **opcionales**
  (se dejan en blanco / `NULL` si no se conocen; se pueden rellenar después en la ficha del equipo).

## 5. Flujo de importación

1. El usuario selecciona o crea el **despliegue** y sube el Excel (`multipart`).
2. El backend **detecta el formato** (tabular con fila de cabecera vs. hoja-por-equipo) y parsea con Apache POI.
3. **Validación**: hostname / serie / IP duplicadas (dentro del fichero y contra la BD) + campos obligatorios.
4. Crea `entidades`, `centros`, `ubicaciones` si no existen; crea `equipos` y `redes`; registra `despliegue_equipos`.
5. Devuelve el **informe**: resumen (creados / actualizados / con error) + lista de errores por fila (motivo) + duplicados.

## 6. Plan de implementación

### Backend
- `pom.xml`: dependencia `org.apache.poi:poi-ooxml`.
- Entidades y repositorios: `Despliegue`, `Entidad`, `DespliegueEquipo`.
- `service/ImportacionService` (o `ExcelImportService`): detección de formato, parseo, validación y persistencia.
- `controller/ImportacionController`:
  - `POST /api/importaciones` (multipart + nombre despliegue) → informe.
  - `GET /api/despliegues` → lista de misiones.
  - `GET /api/despliegues/{id}/equipos` → equipos de la misión con estado renove.
  - `GET /api/importaciones/{id}/informe` → informe de una importación.
- DTOs: `ImportacionResult` (resumen + errores), `DespliegueResponse`, `DespliegueEquipoResponse`.
- Migración `V3__despliegues.sql`.
- Duplicados: consultas por `hostname`, `numero_serie`, `etiqueta_patrimonial`, `redes.ip`.

### Frontend
- Página **Importación**: crear/ver despliegues, subir el fichero, mostrar el informe (resumen + tabla de errores).
- Página **Despliegue**: listado de equipos de la misión con estado renove (`R`/`E`/`H`), año y perfil de imagen.

## 7. Pendientes de confirmar

1. ~~`MAQUETA B2`~~ → **Resuelto**: es el nombre de la imagen con la que se maquetan los equipos
   (la del pendrive). Se guarda como texto en `despliegue_equipos.perfil_imagen` (ej. `B2`).
2. ~~La `R` de `(R/H/E)`~~ → **Resuelto**: siempre con año (`R2026`), el año se extrae del valor.
3. ~~`Entitat`~~ → **Resuelto**: un único valor `Root entity > Informàtica` en el fichero. Se crea una
   `entidades` con `entidad_raiz='Root entity'` y `nombre='Informàtica'`.
4. ~~Etiqueta patrimonial~~ → **Resuelto**: no existe en `Relació…`/`CAPs`; solo en la plantilla IDI
   (`LT2B`/`GC`). El dedupe por etiqueta aplica solo a importaciones IDI.
