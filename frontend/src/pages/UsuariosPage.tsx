import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputLabel,
  ListItemText,
  MenuItem,
  Paper,
  Select,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import { Add, Assignment, Delete, Edit, FolderCopy } from '@mui/icons-material'
import { useForm } from 'react-hook-form'
import { centrosApi, importacionesApi, usuariosApi, type Usuario } from '../lib/api'

type Formulario = {
  username: string
  password: string
  nombreCompleto: string
  email: string
  rol: string
}

function rolColor(roles: string[]): 'secondary' | 'primary' | 'default' {
  if (roles.includes('ADMIN')) return 'secondary'
  if (roles.includes('TECNICO')) return 'primary'
  return 'default'
}

export default function UsuariosPage() {
  const queryClient = useQueryClient()
  const [crearAbierto, setCrearAbierto] = useState(false)
  const [editar, setEditar] = useState<Usuario | null>(null)
  const [asignarCentrosDe, setAsignarCentrosDe] = useState<Usuario | null>(null)
  const [asignarProyectosDe, setAsignarProyectosDe] = useState<Usuario | null>(null)
  const [centrosSel, setCentrosSel] = useState<number[]>([])
  const [proyectosSel, setProyectosSel] = useState<number[]>([])
  const [error, setError] = useState('')

  const { data: usuarios = [], isLoading } = useQuery({
    queryKey: ['usuarios'],
    queryFn: usuariosApi.listar,
  })
  const { data: centros = [] } = useQuery({
    queryKey: ['centros'],
    queryFn: centrosApi.list,
  })
  const { data: despliegues = [] } = useQuery({
    queryKey: ['despliegues'],
    queryFn: importacionesApi.listarDespliegues,
  })

  const { register, handleSubmit, reset, setValue } = useForm<Formulario>({
    defaultValues: { username: '', password: '', nombreCompleto: '', email: '', rol: 'TECNICO' },
  })

  const invalidar = () => {
    queryClient.invalidateQueries({ queryKey: ['usuarios'] })
    queryClient.invalidateQueries({ queryKey: ['despliegues'] })
    queryClient.invalidateQueries({ queryKey: ['dashboard'] })
  }

  const crearMutation = useMutation({
    mutationFn: (f: Formulario) =>
      usuariosApi.crear({
        username: f.username,
        password: f.password,
        nombreCompleto: f.nombreCompleto,
        email: f.email || undefined,
        rol: f.rol,
      }),
    onSuccess: () => {
      invalidar()
      setCrearAbierto(false)
      reset()
    },
    onError: (e) => {
      const data = (e as { response?: { data?: { error?: string } } }).response?.data
      setError(data?.error ?? 'Error al crear el usuario')
    },
  })

  const editarMutation = useMutation({
    mutationFn: (f: Formulario) =>
      usuariosApi.actualizar(editar!.id, {
        nombreCompleto: f.nombreCompleto,
        email: f.email || undefined,
        rol: f.rol,
      }),
    onSuccess: () => {
      invalidar()
      setEditar(null)
    },
    onError: (e) => {
      const data = (e as { response?: { data?: { error?: string } } }).response?.data
      setError(data?.error ?? 'Error al actualizar el usuario')
    },
  })

  const eliminarMutation = useMutation({
    mutationFn: (id: number) => usuariosApi.eliminar(id),
    onSuccess: invalidar,
  })

  const activarMutation = useMutation({
    mutationFn: (u: Usuario) =>
      usuariosApi.actualizar(u.id, {
        nombreCompleto: u.nombreCompleto,
        email: u.email ?? undefined,
        activo: !u.activo,
      }),
    onSuccess: invalidar,
  })

  const asignarCentrosMutation = useMutation({
    mutationFn: (ids: number[]) => usuariosApi.asignarCentros(asignarCentrosDe!.id, ids),
    onSuccess: () => {
      invalidar()
      setAsignarCentrosDe(null)
    },
  })

  const asignarProyectosMutation = useMutation({
    mutationFn: (ids: number[]) => usuariosApi.asignarDespliegues(asignarProyectosDe!.id, ids),
    onSuccess: () => {
      invalidar()
      setAsignarProyectosDe(null)
    },
  })

  const abrirEditar = (u: Usuario) => {
    setError('')
    setEditar(u)
    setValue('nombreCompleto', u.nombreCompleto)
    setValue('email', u.email ?? '')
    setValue('rol', u.roles[0] ?? 'TECNICO')
    setValue('username', u.username)
    setValue('password', '')
  }

  const abrirCentros = (u: Usuario) => {
    setAsignarCentrosDe(u)
    setCentrosSel(u.centroIds)
  }

  const abrirProyectos = (u: Usuario) => {
    setAsignarProyectosDe(u)
    setProyectosSel(u.despliegueIds)
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>Usuarios</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => { setError(''); setCrearAbierto(true) }}>
          Nuevo usuario
        </Button>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Nombre</TableCell>
              <TableCell>Usuario</TableCell>
              <TableCell>Rol</TableCell>
              <TableCell>Centros asignados</TableCell>
              <TableCell>Proyectos asignados</TableCell>
              <TableCell align="center">Activo</TableCell>
              <TableCell align="right" />
            </TableRow>
          </TableHead>
          <TableBody>
            {usuarios.map((u) => (
              <TableRow key={u.id} hover>
                <TableCell sx={{ fontWeight: 600 }}>{u.nombreCompleto}</TableCell>
                <TableCell>{u.username}</TableCell>
                <TableCell>
                  <Chip size="small" label={u.roles.join(', ')} color={rolColor(u.roles)} />
                </TableCell>
                <TableCell>
                  {u.centroNombres.length > 0 ? u.centroNombres.join(', ') : '—'}
                </TableCell>
                <TableCell>
                  {u.despliegueNombres.length > 0 ? u.despliegueNombres.join(', ') : '—'}
                </TableCell>
                <TableCell align="center">
                  <Switch checked={u.activo} size="small" onChange={() => activarMutation.mutate(u)} />
                </TableCell>
                <TableCell align="right">
                  <IconButton size="small" title="Editar" onClick={() => abrirEditar(u)}>
                    <Edit fontSize="small" />
                  </IconButton>
                  <IconButton size="small" title="Centros" onClick={() => abrirCentros(u)}>
                    <Assignment fontSize="small" />
                  </IconButton>
                  <IconButton size="small" title="Proyectos" onClick={() => abrirProyectos(u)}>
                    <FolderCopy fontSize="small" />
                  </IconButton>
                  <IconButton size="small" color="error" title="Eliminar" onClick={() => eliminarMutation.mutate(u.id)}>
                    <Delete fontSize="small" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!isLoading && usuarios.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No hay usuarios.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* CREAR */}
      <Dialog open={crearAbierto} onClose={() => setCrearAbierto(false)} maxWidth="xs" fullWidth>
        <DialogTitle>Nuevo usuario</DialogTitle>
        <DialogContent>
          <Box component="form" onSubmit={handleSubmit((f) => crearMutation.mutate(f))} sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField label="Nombre completo" {...register('nombreCompleto')} required />
            <TextField label="Usuario (DNI / NIE)" {...register('username')} required />
            <TextField label="Contraseña" type="password" {...register('password')} required />
            <TextField label="Email" {...register('email')} />
            <FormControl fullWidth>
              <InputLabel>Rol</InputLabel>
              <Select label="Rol" defaultValue="TECNICO" {...register('rol')}>
                <MenuItem value="ADMIN">Admin</MenuItem>
                <MenuItem value="TECNICO">Técnico</MenuItem>
                <MenuItem value="CONSULTA">Consulta</MenuItem>
              </Select>
            </FormControl>
            {error && <Alert severity="error">{error}</Alert>}
            <DialogActions sx={{ px: 0 }}>
              <Button onClick={() => setCrearAbierto(false)}>Cancelar</Button>
              <Button type="submit" variant="contained" disabled={crearMutation.isPending}>
                Crear
              </Button>
            </DialogActions>
          </Box>
        </DialogContent>
      </Dialog>

      {/* EDITAR */}
      <Dialog open={!!editar} onClose={() => setEditar(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Editar {editar?.nombreCompleto}</DialogTitle>
        <DialogContent>
          <Box component="form" onSubmit={handleSubmit((f) => editarMutation.mutate(f))} sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField label="Nombre completo" {...register('nombreCompleto')} required />
            <TextField label="Email" {...register('email')} />
            <FormControl fullWidth>
              <InputLabel>Rol</InputLabel>
              <Select label="Rol" {...register('rol')}>
                <MenuItem value="ADMIN">Admin</MenuItem>
                <MenuItem value="TECNICO">Técnico</MenuItem>
                <MenuItem value="CONSULTA">Consulta</MenuItem>
              </Select>
            </FormControl>
            {error && <Alert severity="error">{error}</Alert>}
            <DialogActions sx={{ px: 0 }}>
              <Button onClick={() => setEditar(null)}>Cancelar</Button>
              <Button type="submit" variant="contained" disabled={editarMutation.isPending}>
                Guardar
              </Button>
            </DialogActions>
          </Box>
        </DialogContent>
      </Dialog>

      {/* ASIGNAR CENTROS */}
      <Dialog open={!!asignarCentrosDe} onClose={() => setAsignarCentrosDe(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Centros de {asignarCentrosDe?.nombreCompleto}</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mt: 1 }}>
            <InputLabel>Centros</InputLabel>
            <Select
              multiple
              label="Centros"
              value={centrosSel}
              onChange={(e) => setCentrosSel(typeof e.target.value === 'string' ? [] : e.target.value)}
              renderValue={(sel) => centros.filter((c) => sel.includes(c.id)).map((c) => c.nombre).join(', ')}
            >
              {centros.map((c) => (
                <MenuItem key={c.id} value={c.id}>
                  <ListItemText primary={c.nombre} secondary={c.codigo} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <DialogActions sx={{ px: 0, pt: 2 }}>
            <Button onClick={() => setAsignarCentrosDe(null)}>Cancelar</Button>
            <Button variant="contained" disabled={asignarCentrosMutation.isPending} onClick={() => asignarCentrosMutation.mutate(centrosSel)}>
              Guardar
            </Button>
          </DialogActions>
        </DialogContent>
      </Dialog>

      {/* ASIGNAR PROYECTOS */}
      <Dialog open={!!asignarProyectosDe} onClose={() => setAsignarProyectosDe(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Proyectos de {asignarProyectosDe?.nombreCompleto}</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mt: 1 }}>
            <InputLabel>Proyectos</InputLabel>
            <Select
              multiple
              label="Proyectos"
              value={proyectosSel}
              onChange={(e) => setProyectosSel(typeof e.target.value === 'string' ? [] : e.target.value)}
              renderValue={(sel) => despliegues.filter((d) => sel.includes(d.id)).map((d) => d.nombre).join(', ')}
            >
              {despliegues.map((d) => (
                <MenuItem key={d.id} value={d.id}>
                  <ListItemText primary={d.nombre} secondary={d.provincia ?? '—'} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <DialogActions sx={{ px: 0, pt: 2 }}>
            <Button onClick={() => setAsignarProyectosDe(null)}>Cancelar</Button>
            <Button variant="contained" disabled={asignarProyectosMutation.isPending} onClick={() => asignarProyectosMutation.mutate(proyectosSel)}>
              Guardar
            </Button>
          </DialogActions>
        </DialogContent>
      </Dialog>
    </Box>
  )
}
