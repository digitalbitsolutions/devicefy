# Frontend de Devicefy

Cliente React/TypeScript para la administración del inventario, proyectos y trabajo técnico de Devicefy.

## Stack

- React 19 y React Router.
- TypeScript 6 y Vite 8.
- Material UI 9.
- TanStack React Query para estado remoto.
- Axios para `/api`.
- React Hook Form y Recharts.
- Oxlint.

## Desarrollo

El backend debe estar disponible en `http://localhost:8080`.

```powershell
npm install
npm run dev
```

La aplicación queda disponible en `http://localhost:5173`.

## Comandos

```powershell
npm run dev      # servidor Vite
npm run build    # TypeScript + build de producción
npm run lint     # análisis estático
npm run preview  # sirve el build generado
```

## Rutas principales

| Ruta | Uso |
|---|---|
| `/login` | Autenticación |
| `/` | Dashboard |
| `/equipos` | Inventario, filtros y paginación |
| `/centros` | Centros y responsables |
| `/ubicaciones` | Ubicaciones |
| `/importacion` | Importación Excel |
| `/despliegues` | Administración de proyectos |
| `/despliegues/:id` | Equipos de un proyecto |
| `/usuarios` | Usuarios y asignaciones |
| `/informes` | Pantalla provisional |
| `/configuracion` | Pantalla provisional |

## Convenciones relevantes

- Las llamadas de servidor se centralizan en `src/lib/api.ts` y se cachean con React Query.
- Las páginas están en `src/pages`; el menú y la visibilidad por rol se gestionan en `src/components/Layout.tsx`.
- Los selectores múltiples mantienen el desplegable abierto mientras se marcan opciones y se cierran con `OK`.
- La persistencia definitiva de un formulario se realiza con su botón `Crear` o `Guardar`.
- Antes de entregar cambios se deben ejecutar `npm run build` y `npm run lint`.

## Pendiente técnico

El build actual es correcto, pero Vite avisa que el chunk principal supera 500 kB. La siguiente optimización de frontend debería cargar las páginas por ruta mediante `lazy`/`Suspense`.
