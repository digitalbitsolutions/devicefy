import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  ListItemText,
  ListSubheader,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import {
  ArrowForward,
  Add,
  Assignment,
  Delete,
  Edit,
} from '@mui/icons-material'
import { authApi, centrosApi, importacionesApi, usuariosApi, type Despliegue } from '../lib/api'
import { COMUNIDADES_AUTONOMAS, ESTADOS_PROYECTO, PROVINCIAS_POR_CCAA } from '../lib/constants'

const chipColor = (estado: string) => {
  if (estado === 'HECHO' || estado === 'FINALIZADO') return 'success'
  if (estado === 'EN_PROCESO' || estado === 'PROCESANDO') return 'primary'
  return 'default'
}

type FormProyecto = {
  nombre: string
  provincia: string
  comunidadAutonoma: string
  estado: string
}

export default function DesplieguesPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [crearAbierto, setCrearAbierto] = useState(false)
  const [editar, setEditar] = useState<Despliegue | null>(null)
  const [asignarTecnicosDe, setAsignarTecnicosDe] = useState<Despliegue | null>(null)
  const [error, setError] = useState('')
  const [ca, setCa] = useState('')
  const [centrosSel, setCentrosSel] = useState<number[]>([])
  const [tecnicosSel, setTecnicosSel] = useState<number[]>([])
  const [nombreCrear, setNombreCrear] = useState('')
  const [provinciaCrear, setProvinciaCrear] = useState('')
  const [estadoCrear, setEstadoCrear] = useState('PENDIENTE')
  const [nombreEditar, setNombreEditar] = useState('')
  const [provinciaEditar, setProvinciaEditar] = useState('')
  const [estadoEditar, setEstadoEditar] = useState('PENDIENTE')
  const [selectorMultipleAbierto, setSelectorMultipleAbierto] = useState('')

  const { data: despliegues = [], isLoading } = useQuery({
    queryKey: ['despliegues'],
    queryFn: importacionesApi.listarDespliegues,
  })

  const { data: authMe } = useQuery({ queryKey: ['authMe'], queryFn: authApi.me })
  const esAdmin = authMe?.authorities.includes('ROLE_ADMIN') ?? false

  const { data: centros = [] } = useQuery({
    queryKey: ['centros'],
    queryFn: () => centrosApi.list(),
  })
  const centrosDisponibles = [...centros]
    .filter((centro) => centro.activo)
    .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'))

  const { data: usuarios = [] } = useQuery({
    queryKey: ['usuarios'],
    queryFn: usuariosApi.listar,
    enabled: esAdmin,
  })
  const tecnicosDisponibles = [...usuarios]
    .filter((usuario) => usuario.activo && usuario.roles.includes('TECNICO'))
    .sort((a, b) => a.nombreCompleto.localeCompare(b.nombreCompleto, 'es'))
  const provinciasDisponibles = ca ? (PROVINCIAS_POR_CCAA[ca] ?? []) : []

  const invalidar = () => {
    queryClient.invalidateQueries({ queryKey: ['despliegues'] })
    queryClient.invalidateQueries({ queryKey: ['dashboard'] })
  }

  const crearMutation = useMutation({
    mutationFn: (f: FormProyecto) =>
      importacionesApi.crearDespliegue({
        nombre: f.nombre,
        provincia: f.provincia,
        comunidadAutonoma: f.comunidadAutonoma,
        centroIds: centrosSel,
        tecnicoIds: tecnicosSel,
        estado: f.estado,
      }),
    onSuccess: () => {
      invalidar()
      setCrearAbierto(false)
      setCentrosSel([])
      setTecnicosSel([])
      setCa('')
      setNombreCrear('')
      setProvinciaCrear('')
      setEstadoCrear('PENDIENTE')
    },
    onError: (e) => {
      const data = (e as { response?: { data?: { error?: string } } }).response?.data
      setError(data?.error ?? 'Error al crear el proyecto')
    },
  })

  const actualizarMutation = useMutation({
    mutationFn: (f: FormProyecto) =>
      importacionesApi.actualizar(editar!.id, {
        nombre: f.nombre,
        provincia: f.provincia || undefined,
        comunidadAutonoma: f.comunidadAutonoma || undefined,
        estado: f.estado,
        centroIds: centrosSel,
        tecnicoIds: tecnicosSel,
      }),
    onSuccess: () => {
      invalidar()
      setEditar(null)
    },
    onError: (e) => {
      const data = (e as { response?: { data?: { error?: string } } }).response?.data
      setError(data?.error ?? 'Error al guardar el proyecto')
    },
  })

  const eliminarMutation = useMutation({
    mutationFn: (id: number) => importacionesApi.eliminar(id),
    onSuccess: invalidar,
  })

  const asignarTecnicosMutation = useMutation({
    mutationFn: (ids: number[]) => importacionesApi.asignarTecnicos(asignarTecnicosDe!.id, ids),
    onSuccess: () => {
      invalidar()
      setAsignarTecnicosDe(null)
    },
  })

  const abrirCrear = () => {
    setError('')
    setCa('')
    setCentrosSel([])
    setTecnicosSel([])
    setNombreCrear('')
    setProvinciaCrear('')
    setEstadoCrear('PENDIENTE')
    setCrearAbierto(true)
  }

  const abrirEditar = (d: Despliegue) => {
    setError('')
    setEditar(d)
    setCa(d.comunidadAutonoma ?? '')
    setCentrosSel(d.centroIds)
    setTecnicosSel(d.tecnicoIds)
    setNombreEditar(d.nombre)
    setProvinciaEditar(d.provincia ?? '')
    setEstadoEditar(d.estado)
  }

  const cambiarComunidad = (valor: string) => {
    setCa(valor)
    if (crearAbierto) {
      setProvinciaCrear('')
    }
    if (editar) {
      setProvinciaEditar('')
    }
  }

  const eliminar = (d: Despliegue) => {
    if (window.confirm(`¿Eliminar el proyecto "${d.nombre}"?`)) {
      eliminarMutation.mutate(d.id)
    }
  }

  const botonConfirmarSeleccion = () => (
    <ListSubheader
      sx={{
        position: 'sticky',
        top: 'auto',
        bottom: 0,
        zIndex: 1,
        bgcolor: 'background.paper',
        borderTop: 1,
        borderColor: 'divider',
        px: 1,
        py: 1,
        lineHeight: 'normal',
      }}
    >
      <Button
        fullWidth
        size="small"
        variant="contained"
        onClick={(event) => {
          event.stopPropagation()
          setSelectorMultipleAbierto('')
        }}
      >
        OK
      </Button>
    </ListSubheader>
  )

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>Proyectos</Typography>
        {esAdmin && (
          <Button variant="contained" startIcon={<Add />} onClick={abrirCrear}>
            Nuevo proyecto
          </Button>
        )}
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Nombre</TableCell>
              <TableCell>Comunidad</TableCell>
              <TableCell>Centros</TableCell>
              <TableCell>Técnicos</TableCell>
              <TableCell align="center">Equipos</TableCell>
              <TableCell align="center">Hechos</TableCell>
              <TableCell>Estado</TableCell>
              <TableCell align="right" />
            </TableRow>
          </TableHead>
          <TableBody>
            {despliegues.map((d) => (
              <TableRow key={d.id} hover>
                <TableCell sx={{ fontWeight: 600 }}>{d.nombre}</TableCell>
                <TableCell>{d.comunidadAutonoma ?? '—'}</TableCell>
                <TableCell>
                  {d.centroNombres.length > 0 ? d.centroNombres.join(', ') : '—'}
                </TableCell>
                <TableCell>
                  {d.tecnicoNombres.length > 0 ? d.tecnicoNombres.join(', ') : '—'}
                </TableCell>
                <TableCell align="center">{d.totalEquipos}</TableCell>
                <TableCell align="center">{d.hechos}</TableCell>
                <TableCell>
                  <Chip size="small" label={d.estado} color={chipColor(d.estado)} />
                </TableCell>
                <TableCell align="right">
                  {esAdmin && (
                    <>
                      <IconButton size="small" title="Editar" onClick={() => abrirEditar(d)}>
                        <Edit fontSize="small" />
                      </IconButton>
                      <IconButton size="small" title="Técnicos" onClick={() => { setAsignarTecnicosDe(d); setTecnicosSel(d.tecnicoIds) }}>
                        <Assignment fontSize="small" />
                      </IconButton>
                      <IconButton size="small" color="error" title="Eliminar" onClick={() => eliminar(d)}>
                        <Delete fontSize="small" />
                      </IconButton>
                    </>
                  )}
                  <IconButton size="small" title="Ver equipos" onClick={() => navigate(`/despliegues/${d.id}`)}>
                    <ArrowForward fontSize="small" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!isLoading && despliegues.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No hay proyectos. Crea uno desde "Nuevo proyecto".
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* CREAR */}
      <Dialog open={crearAbierto} onClose={() => setCrearAbierto(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Nuevo proyecto</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Nombre del proyecto"
              required
              fullWidth
              value={nombreCrear}
              onChange={(e) => setNombreCrear(e.target.value)}
            />
            <TextField
              select
              label="Comunidad Autónoma"
              required
              fullWidth
              value={ca}
              onChange={(e) => cambiarComunidad(e.target.value)}
            >
              <MenuItem value="">Selecciona…</MenuItem>
              {COMUNIDADES_AUTONOMAS.map((c) => (
                <MenuItem key={c} value={c}>{c}</MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Provincia"
              required
              fullWidth
              disabled={!ca}
              value={provinciaCrear}
              onChange={(e) => setProvinciaCrear(e.target.value)}
            >
              <MenuItem value="">Selecciona…</MenuItem>
              {provinciasDisponibles.map((provincia) => (
                <MenuItem key={provincia} value={provincia}>{provincia}</MenuItem>
              ))}
            </TextField>
            <FormControl fullWidth>
              <InputLabel>Centros existentes</InputLabel>
              <Select
                multiple
                label="Centros existentes"
                open={selectorMultipleAbierto === 'crear-centros'}
                onOpen={() => setSelectorMultipleAbierto('crear-centros')}
                onClose={() => setSelectorMultipleAbierto('')}
                value={centrosSel}
                onChange={(e) => setCentrosSel(typeof e.target.value === 'string' ? [] : e.target.value)}
                renderValue={(sel) => centros.filter((c) => sel.includes(c.id)).map((c) => c.nombre).join(', ')}
              >
                {centrosDisponibles.length === 0 ? (
                  <MenuItem disabled>No hay centros disponibles. Crea uno desde Centros.</MenuItem>
                ) : (
                  centrosDisponibles.map((c) => (
                    <MenuItem key={c.id} value={c.id}>
                      <Checkbox checked={centrosSel.includes(c.id)} />
                      <ListItemText primary={c.nombre} secondary={c.provincia ?? ''} />
                    </MenuItem>
                  ))
                )}
                {botonConfirmarSeleccion()}
              </Select>
            </FormControl>
            <FormControl fullWidth>
              <InputLabel>Técnicos</InputLabel>
              <Select
                multiple
                label="Técnicos"
                open={selectorMultipleAbierto === 'crear-tecnicos'}
                onOpen={() => setSelectorMultipleAbierto('crear-tecnicos')}
                onClose={() => setSelectorMultipleAbierto('')}
                value={tecnicosSel}
                onChange={(e) => setTecnicosSel(typeof e.target.value === 'string' ? [] : e.target.value)}
                renderValue={(sel) => usuarios.filter((u) => sel.includes(u.id)).map((u) => u.nombreCompleto).join(', ')}
              >
                {tecnicosDisponibles.length === 0 ? (
                  <MenuItem disabled>No hay técnicos activos disponibles.</MenuItem>
                ) : (
                  tecnicosDisponibles.map((tecnico) => (
                    <MenuItem key={tecnico.id} value={tecnico.id}>
                      <Checkbox checked={tecnicosSel.includes(tecnico.id)} />
                      <ListItemText primary={tecnico.nombreCompleto} secondary={tecnico.username} />
                    </MenuItem>
                  ))
                )}
                {botonConfirmarSeleccion()}
              </Select>
            </FormControl>
            <TextField
              select
              label="Estado"
              fullWidth
              value={estadoCrear}
              onChange={(e) => setEstadoCrear(e.target.value)}
            >
              {ESTADOS_PROYECTO.map((estado) => (
                <MenuItem key={estado} value={estado}>{estado}</MenuItem>
              ))}
            </TextField>
            {error && <Alert severity="error">{error}</Alert>}
            <DialogActions sx={{ px: 0 }}>
              <Button onClick={() => setCrearAbierto(false)}>Cancelar</Button>
              <Button
                variant="contained"
                disabled={crearMutation.isPending || !nombreCrear.trim() || !ca || !provinciaCrear}
                onClick={() =>
                  crearMutation.mutate({ nombre: nombreCrear, provincia: provinciaCrear, comunidadAutonoma: ca, estado: estadoCrear })
                }
              >
                Crear
              </Button>
            </DialogActions>
          </Box>
        </DialogContent>
      </Dialog>

      {/* EDITAR */}
      <Dialog open={!!editar} onClose={() => setEditar(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Editar {editar?.nombre}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Nombre"
              fullWidth
              value={nombreEditar}
              onChange={(e) => setNombreEditar(e.target.value)}
            />
            <TextField
              select
              label="Comunidad Autónoma"
              required
              fullWidth
              value={ca}
              onChange={(e) => cambiarComunidad(e.target.value)}
            >
              <MenuItem value="">Selecciona…</MenuItem>
              {COMUNIDADES_AUTONOMAS.map((c) => (
                <MenuItem key={c} value={c}>{c}</MenuItem>
              ))}
            </TextField>
            <TextField
              select
              label="Provincia"
              required
              fullWidth
              disabled={!ca}
              value={provinciaEditar}
              onChange={(e) => setProvinciaEditar(e.target.value)}
            >
              <MenuItem value="">Selecciona…</MenuItem>
              {provinciasDisponibles.map((provincia) => (
                <MenuItem key={provincia} value={provincia}>{provincia}</MenuItem>
              ))}
            </TextField>
            <FormControl fullWidth>
              <InputLabel>Centros existentes</InputLabel>
              <Select
                multiple
                label="Centros existentes"
                open={selectorMultipleAbierto === 'editar-centros'}
                onOpen={() => setSelectorMultipleAbierto('editar-centros')}
                onClose={() => setSelectorMultipleAbierto('')}
                value={centrosSel}
                onChange={(e) => setCentrosSel(typeof e.target.value === 'string' ? [] : e.target.value)}
                renderValue={(sel) => centros.filter((c) => sel.includes(c.id)).map((c) => c.nombre).join(', ')}
              >
                {centrosDisponibles.length === 0 ? (
                  <MenuItem disabled>No hay centros disponibles.</MenuItem>
                ) : (
                  centrosDisponibles.map((c) => (
                    <MenuItem key={c.id} value={c.id}>
                      <Checkbox checked={centrosSel.includes(c.id)} />
                      <ListItemText primary={c.nombre} secondary={c.provincia ?? ''} />
                    </MenuItem>
                  ))
                )}
                {botonConfirmarSeleccion()}
              </Select>
            </FormControl>
            <FormControl fullWidth>
              <InputLabel>Técnicos</InputLabel>
              <Select
                multiple
                label="Técnicos"
                open={selectorMultipleAbierto === 'editar-tecnicos'}
                onOpen={() => setSelectorMultipleAbierto('editar-tecnicos')}
                onClose={() => setSelectorMultipleAbierto('')}
                value={tecnicosSel}
                onChange={(e) => setTecnicosSel(typeof e.target.value === 'string' ? [] : e.target.value)}
                renderValue={(sel) => usuarios.filter((u) => sel.includes(u.id)).map((u) => u.nombreCompleto).join(', ')}
              >
                {tecnicosDisponibles.length === 0 ? (
                  <MenuItem disabled>No hay técnicos activos disponibles.</MenuItem>
                ) : (
                  tecnicosDisponibles.map((tecnico) => (
                    <MenuItem key={tecnico.id} value={tecnico.id}>
                      <Checkbox checked={tecnicosSel.includes(tecnico.id)} />
                      <ListItemText primary={tecnico.nombreCompleto} secondary={tecnico.username} />
                    </MenuItem>
                  ))
                )}
                {botonConfirmarSeleccion()}
              </Select>
            </FormControl>
            <TextField
              select
              label="Estado"
              fullWidth
              value={estadoEditar}
              onChange={(e) => setEstadoEditar(e.target.value)}
            >
              {ESTADOS_PROYECTO.map((estado) => (
                <MenuItem key={estado} value={estado}>{estado}</MenuItem>
              ))}
            </TextField>
            {error && <Alert severity="error">{error}</Alert>}
            <DialogActions sx={{ px: 0 }}>
              <Button onClick={() => setEditar(null)}>Cancelar</Button>
              <Button
                variant="contained"
                disabled={actualizarMutation.isPending || !nombreEditar.trim() || !ca || !provinciaEditar}
                onClick={() =>
                  actualizarMutation.mutate({ nombre: nombreEditar, provincia: provinciaEditar, comunidadAutonoma: ca, estado: estadoEditar })
                }
              >
                Guardar
              </Button>
            </DialogActions>
          </Box>
        </DialogContent>
      </Dialog>

      {/* ASIGNAR TÉCNICOS */}
      <Dialog open={!!asignarTecnicosDe} onClose={() => setAsignarTecnicosDe(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Técnicos de {asignarTecnicosDe?.nombre}</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mt: 1 }}>
            <InputLabel>Técnicos</InputLabel>
            <Select
              multiple
              label="Técnicos"
              open={selectorMultipleAbierto === 'asignar-tecnicos'}
              onOpen={() => setSelectorMultipleAbierto('asignar-tecnicos')}
              onClose={() => setSelectorMultipleAbierto('')}
              value={tecnicosSel}
              onChange={(e) => setTecnicosSel(typeof e.target.value === 'string' ? [] : e.target.value)}
              renderValue={(sel) => usuarios.filter((u) => sel.includes(u.id)).map((u) => u.nombreCompleto).join(', ')}
            >
              {tecnicosDisponibles.map((tecnico) => (
                <MenuItem key={tecnico.id} value={tecnico.id}>
                  <Checkbox checked={tecnicosSel.includes(tecnico.id)} />
                  <ListItemText primary={tecnico.nombreCompleto} secondary={tecnico.username} />
                </MenuItem>
              ))}
              {botonConfirmarSeleccion()}
            </Select>
          </FormControl>
          <DialogActions sx={{ px: 0, pt: 2 }}>
            <Button onClick={() => setAsignarTecnicosDe(null)}>Cancelar</Button>
            <Button variant="contained" disabled={asignarTecnicosMutation.isPending} onClick={() => asignarTecnicosMutation.mutate(tecnicosSel)}>
              Guardar
            </Button>
          </DialogActions>
        </DialogContent>
      </Dialog>
    </Box>
  )
}
