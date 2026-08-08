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
  InputLabel,
  ListItemText,
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
import { Add, Assignment } from '@mui/icons-material'
import { useForm } from 'react-hook-form'
import { centrosApi, usuariosApi, type Usuario } from '../lib/api'

type Formulario = {
  username: string
  password: string
  nombreCompleto: string
  email: string
  rol: string
}

export default function UsuariosPage() {
  const queryClient = useQueryClient()
  const [crearAbierto, setCrearAbierto] = useState(false)
  const [asignarUsuario, setAsignarUsuario] = useState<Usuario | null>(null)
  const [centrosSel, setCentrosSel] = useState<number[]>([])
  const [error, setError] = useState('')

  const { data: usuarios = [], isLoading } = useQuery({
    queryKey: ['usuarios'],
    queryFn: usuariosApi.listar,
  })
  const { data: centros = [] } = useQuery({
    queryKey: ['centros'],
    queryFn: centrosApi.list,
  })

  const { register, handleSubmit, reset } = useForm<Formulario>({
    defaultValues: { username: '', password: '', nombreCompleto: '', email: '', rol: 'TECNICO' },
  })

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
      queryClient.invalidateQueries({ queryKey: ['usuarios'] })
      setCrearAbierto(false)
      reset()
    },
    onError: (e) => {
      const data = (e as { response?: { data?: { error?: string } } }).response?.data
      setError(data?.error ?? 'Error al crear el usuario')
    },
  })

  const asignarMutation = useMutation({
    mutationFn: (ids: number[]) => {
      if (!asignarUsuario) return Promise.reject(new Error('Sin usuario'))
      return usuariosApi.asignarCentros(asignarUsuario.id, ids)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] })
      setAsignarUsuario(null)
    },
  })

  const abrirAsignar = (u: Usuario) => {
    setAsignarUsuario(u)
    setCentrosSel(u.centroIds)
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5">Usuarios</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => { setError(''); setCrearAbierto(true) }}>
          Nuevo usuario
        </Button>
      </Box>
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Nombre</TableCell>
              <TableCell>Usuario</TableCell>
              <TableCell>Rol</TableCell>
              <TableCell>Centros asignados</TableCell>
              <TableCell align="center">Activo</TableCell>
              <TableCell align="right" />
            </TableRow>
          </TableHead>
          <TableBody>
            {usuarios.map((u) => (
              <TableRow key={u.id} hover>
                <TableCell>{u.nombreCompleto}</TableCell>
                <TableCell>{u.username}</TableCell>
                <TableCell>
                  <Chip size="small" label={u.roles.join(', ')} color={u.roles.includes('ADMIN') ? 'secondary' : u.roles.includes('TECNICO') ? 'primary' : 'default'} />
                </TableCell>
                <TableCell>
                  {u.centroNombres.length > 0 ? u.centroNombres.join(', ') : '—'}
                </TableCell>
                <TableCell align="center">{u.activo ? 'Sí' : 'No'}</TableCell>
                <TableCell align="right">
                  <Button size="small" startIcon={<Assignment />} onClick={() => abrirAsignar(u)}>
                    Centros
                  </Button>
                </TableCell>
              </TableRow>
            ))}
            {!isLoading && usuarios.length === 0 && (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No hay usuarios.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

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

      <Dialog open={!!asignarUsuario} onClose={() => setAsignarUsuario(null)} maxWidth="xs" fullWidth>
        <DialogTitle>Centros de {asignarUsuario?.nombreCompleto}</DialogTitle>
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
            <Button onClick={() => setAsignarUsuario(null)}>Cancelar</Button>
            <Button variant="contained" disabled={asignarMutation.isPending} onClick={() => asignarMutation.mutate(centrosSel)}>
              Guardar
            </Button>
          </DialogActions>
        </DialogContent>
      </Dialog>
    </Box>
  )
}