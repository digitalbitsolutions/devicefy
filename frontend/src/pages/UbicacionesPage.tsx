import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  IconButton,
  MenuItem,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import { Add, Delete, Edit } from '@mui/icons-material'
import { useForm } from 'react-hook-form'
import { centrosApi, ubicacionesApi, type Ubicacion } from '../lib/api'

interface FormValues {
  centroId: number
  nombre: string
  planta: string
  zona: string
  activo: boolean
}

function errorMessage(e: unknown): string {
  const data = (e as { response?: { data?: { error?: string } } }).response?.data
  return data?.error ?? 'Error al guardar'
}

export default function UbicacionesPage() {
  const queryClient = useQueryClient()
  const [centroFiltro, setCentroFiltro] = useState<string>('')
  const [open, setOpen] = useState(false)
  const [editId, setEditId] = useState<number | null>(null)
  const [error, setError] = useState('')

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<FormValues>({
    defaultValues: { centroId: 0, nombre: '', planta: '', zona: '', activo: true },
  })

  const { data: centros = [] } = useQuery({ queryKey: ['centros'], queryFn: centrosApi.list })

  const { data: ubicaciones = [], isLoading } = useQuery({
    queryKey: ['ubicaciones', centroFiltro],
    queryFn: () =>
      ubicacionesApi.list(centroFiltro ? Number(centroFiltro) : undefined),
  })

  const guardar = useMutation({
    mutationFn: (values: FormValues) =>
      editId ? ubicacionesApi.update(editId, values) : ubicacionesApi.create(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ubicaciones'] })
      setOpen(false)
      reset()
      setEditId(null)
    },
    onError: (e) => setError(errorMessage(e)),
  })

  const eliminar = useMutation({
    mutationFn: (id: number) => ubicacionesApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['ubicaciones'] }),
    onError: (e) => setError(errorMessage(e)),
  })

  const abrirCrear = () => {
    setError('')
    setEditId(null)
    reset({ centroId: centros[0]?.id ?? 0, nombre: '', planta: '', zona: '', activo: true })
    setOpen(true)
  }

  const abrirEditar = (u: Ubicacion) => {
    setError('')
    setEditId(u.id)
    reset({ centroId: u.centroId, nombre: u.nombre, planta: u.planta ?? '', zona: u.zona ?? '', activo: u.activo })
    setOpen(true)
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, gap: 2 }}>
        <Typography variant="h5" sx={{ flexGrow: 1 }}>
          Ubicaciones
        </Typography>
        <TextField
          select
          label="Centro"
          size="small"
          value={centroFiltro}
          onChange={(e) => setCentroFiltro(e.target.value)}
          sx={{ minWidth: 220 }}
        >
          <MenuItem value="">Todos</MenuItem>
          {centros.map((c) => (
            <MenuItem key={c.id} value={String(c.id)}>
              {c.nombre}
            </MenuItem>
          ))}
        </TextField>
        <Button variant="contained" startIcon={<Add />} onClick={abrirCrear}>
          Nueva ubicación
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Centro</TableCell>
              <TableCell>Nombre</TableCell>
              <TableCell>Planta</TableCell>
              <TableCell>Zona</TableCell>
              <TableCell>Activo</TableCell>
              <TableCell align="right">Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading && (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  Cargando…
                </TableCell>
              </TableRow>
            )}
            {!isLoading && ubicaciones.length === 0 && (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  Sin resultados
                </TableCell>
              </TableRow>
            )}
            {ubicaciones.map((u) => (
              <TableRow key={u.id} hover>
                <TableCell>{u.centroNombre}</TableCell>
                <TableCell>{u.nombre}</TableCell>
                <TableCell>{u.planta ?? '—'}</TableCell>
                <TableCell>{u.zona ?? '—'}</TableCell>
                <TableCell>{u.activo ? 'Sí' : 'No'}</TableCell>
                <TableCell align="right">
                  <IconButton size="small" onClick={() => abrirEditar(u)}>
                    <Edit fontSize="small" />
                  </IconButton>
                  <IconButton size="small" color="error" onClick={() => eliminar.mutate(u.id)}>
                    <Delete fontSize="small" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editId ? 'Editar ubicación' : 'Nueva ubicación'}</DialogTitle>
        <DialogContent>
          <Box component="form" noValidate sx={{ mt: 1 }}>
            <TextField
              select
              label="Centro"
              fullWidth
              margin="normal"
              {...register('centroId', { required: 'Selecciona un centro', validate: (v) => v > 0 })}
              error={!!errors.centroId}
              helperText={errors.centroId?.message}
            >
              {centros.map((c) => (
                <MenuItem key={c.id} value={c.id}>
                  {c.nombre}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Nombre"
              fullWidth
              margin="normal"
              {...register('nombre', { required: 'El nombre es obligatorio' })}
              error={!!errors.nombre}
              helperText={errors.nombre?.message}
            />
            <TextField label="Planta" fullWidth margin="normal" {...register('planta')} />
            <TextField label="Zona" fullWidth margin="normal" {...register('zona')} />
            <FormControlLabel
              control={
                <Checkbox
                  defaultChecked
                  {...register('activo')}
                  onChange={(e) => setValue('activo', e.target.checked)}
                />
              }
              label="Activo"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleSubmit((v) => guardar.mutate(v))}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
