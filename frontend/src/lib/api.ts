import axios from 'axios'

export const TOKEN_KEY = 'devicefy_token'

export const getToken = (): string | null => localStorage.getItem(TOKEN_KEY)
export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token)
export const clearToken = () => localStorage.removeItem(TOKEN_KEY)

export const api = axios.create({
  baseURL: '/api',
})

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const url: string = error.config?.url ?? ''
    if (
      error.response?.status === 401 &&
      !url.includes('/auth/login') &&
      !url.includes('/auth/register')
    ) {
      clearToken()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

export interface ApiErrorResponse {
  error?: string
}

export interface AuthResponse {
  token: string
  username: string
  nombreCompleto: string
  roles: string[]
  expiresAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nombreCompleto: string
  email?: string
}

export interface Centro {
  id: number
  codigo: string
  nombre: string
  tipo?: string | null
  direccion?: string | null
  activo: boolean
}

export interface CentroRequest {
  codigo: string
  nombre: string
  tipo?: string
  direccion?: string
  activo?: boolean
}

export interface Ubicacion {
  id: number
  centroId: number
  centroNombre: string
  nombre: string
  planta?: string | null
  zona?: string | null
  activo: boolean
}

export interface UbicacionRequest {
  centroId: number
  nombre: string
  planta?: string
  zona?: string
  activo?: boolean
}

export interface RedConfig {
  id: number
  tipoAsignacion: 'DHCP' | 'ESTATICA'
  ip?: string | null
  mascara?: string | null
  puertaEnlace?: string | null
  dns1?: string | null
  dns2?: string | null
  dominio?: string | null
  actualizadaAt?: string
}

export interface RedConfigRequest {
  tipoAsignacion?: 'DHCP' | 'ESTATICA'
  ip?: string
  mascara?: string
  puertaEnlace?: string
  dns1?: string
  dns2?: string
  dominio?: string
}

export interface Periferico {
  id: number
  equipoId?: number | null
  tipo: string
  marca?: string | null
  modelo?: string | null
  numeroSerie?: string | null
  etiquetaPatrimonial?: string | null
  tamanioPulgadas?: number | null
  activo: boolean
}

export interface Equipo {
  id: number
  hostname?: string | null
  numeroSerie?: string | null
  etiquetaPatrimonial?: string | null
  fabricante?: string | null
  modelo?: string | null
  sistemaOperativo?: string | null
  procesador?: string | null
  tipoEquipo: string
  estado?: string | null
  centroId?: number | null
  centroNombre?: string | null
  ubicacionId?: number | null
  ubicacionNombre?: string | null
  usuarioAsignadoId?: number | null
  usuarioAsignadoNombre?: string | null
  tecnicoProcesoId?: number | null
  tecnicoProcesoNombre?: string | null
  observaciones?: string | null
  activo: boolean
  red?: RedConfig | null
  perifericos: Periferico[]
}

export interface EquipoRequest {
  hostname?: string
  numeroSerie?: string
  etiquetaPatrimonial?: string
  fabricante?: string
  modelo?: string
  sistemaOperativo?: string
  procesador?: string
  tipoEquipo: string
  estado?: string
  centroId?: number
  ubicacionId?: number
  usuarioAsignadoId?: number
  observaciones?: string
  activo?: boolean
  red?: RedConfigRequest | null
}

export interface EquipoFiltros {
  hostname?: string
  numeroSerie?: string
  etiquetaPatrimonial?: string
  estado?: string
  centroId?: number
  activo?: boolean
  tecnicoId?: number
  despliegueId?: number
}

export const authApi = {
  login: (body: LoginRequest) => api.post<AuthResponse>('/auth/login', body).then((r) => r.data),
  register: (body: RegisterRequest) =>
    api.post<AuthResponse>('/auth/register', body).then((r) => r.data),
  me: () => api.get<{ username: string; authorities: string[] }>('/auth/me').then((r) => r.data),
}

export const centrosApi = {
  list: () => api.get<Centro[]>('/centros').then((r) => r.data),
  create: (body: CentroRequest) => api.post<Centro>('/centros', body).then((r) => r.data),
  update: (id: number, body: CentroRequest) =>
    api.put<Centro>(`/centros/${id}`, body).then((r) => r.data),
  remove: (id: number) => api.delete(`/centros/${id}`),
}

export const ubicacionesApi = {
  list: (centroId?: number) =>
    api.get<Ubicacion[]>('/ubicaciones', { params: { centroId } }).then((r) => r.data),
  create: (body: UbicacionRequest) =>
    api.post<Ubicacion>('/ubicaciones', body).then((r) => r.data),
  update: (id: number, body: UbicacionRequest) =>
    api.put<Ubicacion>(`/ubicaciones/${id}`, body).then((r) => r.data),
  remove: (id: number) => api.delete(`/ubicaciones/${id}`),
}

export const equiposApi = {
  list: (filtros: EquipoFiltros = {}) =>
    api.get<Equipo[]>('/equipos', { params: filtros }).then((r) => r.data),
  get: (id: number) => api.get<Equipo>(`/equipos/${id}`).then((r) => r.data),
  create: (body: EquipoRequest) => api.post<Equipo>('/equipos', body).then((r) => r.data),
  update: (id: number, body: EquipoRequest) =>
    api.put<Equipo>(`/equipos/${id}`, body).then((r) => r.data),
  remove: (id: number) => api.delete(`/equipos/${id}`),
}


export interface Despliegue {
  id: number
  nombre: string
  provincia?: string | null
  ficheroNombre?: string | null
  fechaImportacion?: string | null
  estado: string
  totalEquipos: number
  enProceso: number
  hechos: number
  tecnicoIds: number[]
  tecnicoNombres: string[]
}

export interface ActualizarDespliegueRequest {
  nombre?: string
  provincia?: string
  estado?: string
}

export interface ErrorImportacion {
  fila: number
  motivo: string
}

export interface ImportacionResult {
  despliegueId: number
  nombreDespliegue: string
  formato: string
  filasLeidas: number
  equiposCreados: number
  centrosCreados: number
  ubicacionesCreadas: number
  errores: number
  erroresDetalle: ErrorImportacion[]
}

export interface DespliegueEquipo {
  id: number
  despliegueId: number
  equipoId: number
  hostnameActual?: string | null
  hostnameNuevo?: string | null
  estadoRenove?: string | null
  anioRenove?: number | null
  perfilImagen?: string | null
  estado: string
  tecnicoId?: number | null
  tecnicoNombre?: string | null
  fechaToma?: string | null
  numeroSerie?: string | null
  fabricante?: string | null
  modelo?: string | null
  sistemaOperativo?: string | null
  procesador?: string | null
  centroNombre?: string | null
  ubicacionNombre?: string | null
  ip?: string | null
}

export const importacionesApi = {
  importar: (nombreDespliegue: string, archivo: File) => {
    const form = new FormData()
    form.append('nombreDespliegue', nombreDespliegue)
    form.append('archivo', archivo)
    return api.post<ImportacionResult>('/importaciones', form).then((r) => r.data)
  },
  listarDespliegues: () => api.get<Despliegue[]>('/importaciones/despliegues').then((r) => r.data),
  listarEquipos: (id: number) =>
    api.get<DespliegueEquipo[]>(`/importaciones/despliegues/${id}/equipos`).then((r) => r.data),
  actualizar: (id: number, body: ActualizarDespliegueRequest) =>
    api.put<Despliegue>(`/importaciones/despliegues/${id}`, body).then((r) => r.data),
  asignarTecnicos: (id: number, usuarioIds: number[]) =>
    api
      .put<Despliegue>(`/importaciones/despliegues/${id}/tecnicos`, { despliegueIds: usuarioIds })
      .then((r) => r.data),
}


export interface Usuario {
  id: number
  username: string
  nombreCompleto: string
  email?: string | null
  activo: boolean
  roles: string[]
  centroIds: number[]
  centroNombres: string[]
  despliegueIds: number[]
  despliegueNombres: string[]
}

export const usuariosApi = {
  listar: () => api.get<Usuario[]>('/usuarios').then((r) => r.data),
  obtener: (id: number) => api.get<Usuario>(`/usuarios/${id}`).then((r) => r.data),
  crear: (body: {
    username: string
    password: string
    nombreCompleto: string
    email?: string
    rol?: string
  }) => api.post<Usuario>('/usuarios', body).then((r) => r.data),
  actualizar: (
    id: number,
    body: { nombreCompleto: string; email?: string; activo?: boolean; rol?: string },
  ) => api.put<Usuario>(`/usuarios/${id}`, body).then((r) => r.data),
  eliminar: (id: number) => api.delete(`/usuarios/${id}`),
  asignarCentros: (id: number, centroIds: number[]) =>
    api.put<Usuario>(`/usuarios/${id}/centros`, { centroIds }).then((r) => r.data),
  asignarDespliegues: (id: number, despliegueIds: number[]) =>
    api.put<Usuario>(`/usuarios/${id}/despliegues`, { despliegueIds }).then((r) => r.data),
  misCentros: () => api.get<Centro[]>('/usuarios/me/centros').then((r) => r.data),
}

export interface DashboardKpis {
  totalProyectos: number
  totalCentros: number
  totalUbicaciones: number
  totalEquipos: number
  totalUsuarios: number
  equiposPendientes: number
  equiposEnProceso: number
  equiposFinalizados: number
}

export interface DashboardConteo {
  nombre: string
  valor: number
}

export interface DashboardProgresoProyecto {
  id: number
  nombre: string
  provincia?: string | null
  total: number
  enProceso: number
  hechos: number
}

export interface DashboardCargaTecnico {
  id: number
  nombre: string
  asignados: number
  finalizados: number
}

export interface DashboardData {
  kpis: DashboardKpis
  equiposPorEstado: DashboardConteo[]
  equiposPorTipo: DashboardConteo[]
  equiposPorCentro: DashboardConteo[]
  progresoProyectos: DashboardProgresoProyecto[]
  cargaTecnicos: DashboardCargaTecnico[]
}

export const dashboardApi = {
  resumen: () => api.get<DashboardData>('/dashboard').then((r) => r.data),
}
