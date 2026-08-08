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
import { centrosApi, type Centro } from '../lib/api'

interface FormValues {
  codigo: string
  nombre: string
  tipo: string
  direccion: string
  activo: boolean
}

function errorMessage(e: unknown): string {
  const data = (e as { response?: { data?: { error?: string } } }).response?.data
  return data?.error ?? 'Error al guardar'
}

export default function CentrosPage() {
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const [open, setOpen] = useState(false)
  const [editId, setEditId] = useState<number | null>(null)
  const [error, setError] = useState('')

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<FormValues>({
    defaultValues: { codigo: '', nombre: '', tipo: '', direccion: '', activo: true },
  })

  const { data: centros = [], isLoading } = useQuery({
    queryKey: ['centros'],
    queryFn: centrosApi.list,
  })

  const filtrados = centros.filter((c) =>
    `${c.codigo} ${c.nombre}`.toLowerCase().includes(search.toLowerCase()),
  )

  const guardar = useMutation({
    mutationFn: (values: FormValues) =>
      editId ? centrosApi.update(editId, values) : centrosApi.create(values),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['centros'] })
      setOpen(false)
      reset()
      setEditId(null)
    },
    onError: (e) => setError(errorMessage(e)),
  })

  const eliminar = useMutation({
    mutationFn: (id: number) => centrosApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['centros'] }),
    onError: (e) => setError(errorMessage(e)),
  })

  const abrirCrear = () => {
    setError('')
    setEditId(null)
    reset({ codigo: '', nombre: '', tipo: '', direccion: '', activo: true })
    setOpen(true)
  }

  const abrirEditar = (centro: Centro) => {
    setError('')
    setEditId(centro.id)
    reset({
      codigo: centro.codigo,
      nombre: centro.nombre,
      tipo: centro.tipo ?? '',
      direccion: centro.direccion ?? '',
      activo: centro.activo,
    })
    setOpen(true)
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, gap: 2 }}>
        <Typography variant="h5" sx={{ flexGrow: 1 }}>
          Centros
        </Typography>
        <TextField
          label="Buscar"
          size="small"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <Button variant="contained" startIcon={<Add />} onClick={abrirCrear}>
          Nuevo centro
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
              <TableCell>Código</TableCell>
              <TableCell>Nombre</TableCell>
              <TableCell>Tipo</TableCell>
              <TableCell>Dirección</TableCell>
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
            {!isLoading && filtrados.length === 0 && (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  Sin resultados
                </TableCell>
              </TableRow>
            )}
            {filtrados.map((centro) => (
              <TableRow key={centro.id} hover>
                <TableCell>{centro.codigo}</TableCell>
                <TableCell>{centro.nombre}</TableCell>
                <TableCell>{centro.tipo ?? '—'}</TableCell>
                <TableCell>{centro.direccion ?? '—'}</TableCell>
                <TableCell>{centro.activo ? 'Sí' : 'No'}</TableCell>
                <TableCell align="right">
                  <IconButton size="small" onClick={() => abrirEditar(centro)}>
                    <Edit fontSize="small" />
                  </IconButton>
                  <IconButton
                    size="small"
                    color="error"
                    onClick={() => eliminar.mutate(centro.id)}
                  >
                    <Delete fontSize="small" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editId ? 'Editar centro' : 'Nuevo centro'}</DialogTitle>
        <DialogContent>
          <Box component="form" noValidate sx={{ mt: 1 }}>
            <TextField
              label="Código"
              fullWidth
              margin="normal"
              {...register('codigo', { required: 'El código es obligatorio' })}
              error={!!errors.codigo}
              helperText={errors.codigo?.message}
            />
            <TextField
              label="Nombre"
              fullWidth
              margin="normal"
              {...register('nombre', { required: 'El nombre es obligatorio' })}
              error={!!errors.nombre}
              helperText={errors.nombre?.message}
            />
            <TextField label="Tipo" fullWidth margin="normal" {...register('tipo')} />
            <TextField label="Dirección" fullWidth margin="normal" {...register('direccion')} />
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
