import { useState } from 'react'
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
  FormControlLabel,
  IconButton,
  InputLabel,
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
import { Add, Delete, Edit, PersonAddAlt1 } from '@mui/icons-material'
import { useForm } from 'react-hook-form'
import { centrosApi, type Centro, type Responsable, type ResponsableRequest } from '../lib/api'
import { COMUNIDADES_AUTONOMAS, PROVINCIAS_POR_CCAA } from '../lib/constants'

interface FormValues {
  codigo: string
  nombre: string
  tipo: string
  direccion: string
  comunidadAutonoma: string
  provincia: string
  telefono: string
  email: string
  activo: boolean
}

interface RespForm {
  areaOficina: string
  nombre: string
  telefono: string
  email: string
}

function errorMessage(e: unknown): string {
  const data = (e as { response?: { data?: { error?: string } } }).response?.data
  return data?.error ?? 'Error al guardar'
}

const formVacio: FormValues = {
  codigo: '',
  nombre: '',
  tipo: '',
  direccion: '',
  comunidadAutonoma: '',
  provincia: '',
  telefono: '',
  email: '',
  activo: true,
}

export default function CentrosPage() {
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const [open, setOpen] = useState(false)
  const [editId, setEditId] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [responsablesDe, setResponsablesDe] = useState<Centro | null>(null)
  const [respAbierto, setRespAbierto] = useState(false)
  const [respEditar, setRespEditar] = useState<Responsable | null>(null)
  const [respForm, setRespForm] = useState<RespForm>({ areaOficina: '', nombre: '', telefono: '', email: '' })

  const { register, handleSubmit, reset, setValue, watch, formState: { errors } } = useForm<FormValues>({
    defaultValues: formVacio,
  })

  const comunidadAutonoma = watch('comunidadAutonoma')
  const provincia = watch('provincia')

  const cambiarComunidad = (valor: string) => {
    setValue('comunidadAutonoma', valor)
    if (provincia && !(PROVINCIAS_POR_CCAA[valor] ?? []).includes(provincia)) {
      setValue('provincia', '')
    }
  }

  const { data: centros = [], isLoading } = useQuery({
    queryKey: ['centros'],
    queryFn: () => centrosApi.list(),
  })

  const filtrados = centros.filter((c) =>
    `${c.codigo} ${c.nombre} ${c.comunidadAutonoma ?? ''} ${c.provincia ?? ''}`
      .toLowerCase()
      .includes(search.toLowerCase()),
  )

  const invalidar = () => {
    queryClient.invalidateQueries({ queryKey: ['centros'] })
    queryClient.invalidateQueries({ queryKey: ['despliegues'] })
    queryClient.invalidateQueries({ queryKey: ['dashboard'] })
  }

  const guardar = useMutation({
    mutationFn: (values: FormValues) =>
      editId ? centrosApi.update(editId, values) : centrosApi.create(values),
    onSuccess: () => {
      invalidar()
      setOpen(false)
      reset(formVacio)
      setEditId(null)
    },
    onError: (e) => setError(errorMessage(e)),
  })

  const eliminar = useMutation({
    mutationFn: (id: number) => centrosApi.remove(id),
    onSuccess: invalidar,
    onError: (e) => setError(errorMessage(e)),
  })

  const guardarResponsable = useMutation({
    mutationFn: (f: RespForm) =>
      respEditar
        ? centrosApi.actualizarResponsable(responsablesDe!.id, respEditar.id, f as ResponsableRequest)
        : centrosApi.crearResponsable(responsablesDe!.id, f as ResponsableRequest),
    onSuccess: () => {
      invalidar()
      setRespAbierto(false)
      setRespEditar(null)
      setRespForm({ areaOficina: '', nombre: '', telefono: '', email: '' })
    },
    onError: (e) => setError(errorMessage(e)),
  })

  const eliminarResponsable = useMutation({
    mutationFn: (rid: number) => centrosApi.eliminarResponsable(responsablesDe!.id, rid),
    onSuccess: invalidar,
    onError: (e) => setError(errorMessage(e)),
  })

  const abrirCrear = () => {
    setError('')
    setEditId(null)
    reset(formVacio)
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
      comunidadAutonoma: centro.comunidadAutonoma ?? '',
      provincia: centro.provincia ?? '',
      telefono: centro.telefono ?? '',
      email: centro.email ?? '',
      activo: centro.activo,
    })
    setOpen(true)
  }

  const abrirNuevoResponsable = () => {
    setRespEditar(null)
    setRespForm({ areaOficina: '', nombre: '', telefono: '', email: '' })
    setRespAbierto(true)
  }

  const abrirEditarResponsable = (r: Responsable) => {
    setRespEditar(r)
    setRespForm({
      areaOficina: r.areaOficina ?? '',
      nombre: r.nombre ?? '',
      telefono: r.telefono ?? '',
      email: r.email ?? '',
    })
    setRespAbierto(true)
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, gap: 2 }}>
        <Typography variant="h5" sx={{ flexGrow: 1, fontWeight: 700 }}>
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
              <TableCell>Comunidad</TableCell>
              <TableCell>Provincia</TableCell>
              <TableCell>Teléfono</TableCell>
              <TableCell align="center">Responsables</TableCell>
              <TableCell align="center">Activo</TableCell>
              <TableCell align="right">Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading && (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  Cargando…
                </TableCell>
              </TableRow>
            )}
            {!isLoading && filtrados.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  Sin resultados
                </TableCell>
              </TableRow>
            )}
            {filtrados.map((centro) => (
              <TableRow key={centro.id} hover>
                <TableCell>{centro.codigo}</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>{centro.nombre}</TableCell>
                <TableCell>{centro.comunidadAutonoma ?? '—'}</TableCell>
                <TableCell>{centro.provincia ?? '—'}</TableCell>
                <TableCell>{centro.telefono ?? '—'}</TableCell>
                <TableCell align="center">
                  <Chip size="small" label={centro.responsables?.length ?? 0} onClick={() => setResponsablesDe(centro)} />
                </TableCell>
                <TableCell align="center">{centro.activo ? 'Sí' : 'No'}</TableCell>
                <TableCell align="right">
                  <IconButton size="small" title="Responsables" onClick={() => setResponsablesDe(centro)}>
                    <PersonAddAlt1 fontSize="small" />
                  </IconButton>
                  <IconButton size="small" title="Editar" onClick={() => abrirEditar(centro)}>
                    <Edit fontSize="small" />
                  </IconButton>
                  <IconButton size="small" color="error" title="Eliminar" onClick={() => eliminar.mutate(centro.id)}>
                    <Delete fontSize="small" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* DIALOG CENTRO */}
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
            <FormControl fullWidth margin="normal">
              <InputLabel>Comunidad Autónoma</InputLabel>
              <Select
                label="Comunidad Autónoma"
                value={comunidadAutonoma}
                onChange={(e) => cambiarComunidad(e.target.value)}
              >
                <MenuItem value="">Selecciona…</MenuItem>
                {COMUNIDADES_AUTONOMAS.map((c) => (
                  <MenuItem key={c} value={c}>{c}</MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl fullWidth margin="normal" disabled={!comunidadAutonoma}>
              <InputLabel>Provincia</InputLabel>
              <Select
                label="Provincia"
                value={provincia}
                onChange={(e) => setValue('provincia', e.target.value)}
              >
                <MenuItem value="">Selecciona…</MenuItem>
                {(PROVINCIAS_POR_CCAA[comunidadAutonoma] ?? []).map((p) => (
                  <MenuItem key={p} value={p}>{p}</MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField label="Dirección" fullWidth margin="normal" {...register('direccion')} />
            <TextField label="Teléfono" fullWidth margin="normal" {...register('telefono')} />
            <TextField label="E-mail" fullWidth margin="normal" {...register('email')} />
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

      {/* DIALOG RESPONSABLES */}
      <Dialog open={!!responsablesDe} onClose={() => setResponsablesDe(null)} fullWidth maxWidth="sm">
        <DialogTitle>Responsables de {responsablesDe?.nombre}</DialogTitle>
        <DialogContent>
          <TableContainer component={Paper} variant="outlined" sx={{ mt: 1 }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Área / Oficina</TableCell>
                  <TableCell>Nombre</TableCell>
                  <TableCell>Teléfono</TableCell>
                  <TableCell align="right" />
                </TableRow>
              </TableHead>
              <TableBody>
                {(responsablesDe?.responsables?.length ?? 0) === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                      Sin responsables.
                    </TableCell>
                  </TableRow>
                ) : (
                  responsablesDe?.responsables?.map((r) => (
                    <TableRow key={r.id} hover>
                      <TableCell>{r.areaOficina ?? '—'}</TableCell>
                      <TableCell>
                        {r.nombre ?? '—'}
                        {r.email && <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>{r.email}</Typography>}
                      </TableCell>
                      <TableCell>{r.telefono ?? '—'}</TableCell>
                      <TableCell align="right">
                        <IconButton size="small" onClick={() => abrirEditarResponsable(r)}>
                          <Edit fontSize="small" />
                        </IconButton>
                        <IconButton size="small" color="error" onClick={() => eliminarResponsable.mutate(r.id)}>
                          <Delete fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <DialogActions sx={{ px: 0, pt: 2 }}>
            <Button onClick={() => setResponsablesDe(null)}>Cerrar</Button>
            <Button variant="contained" startIcon={<Add />} onClick={abrirNuevoResponsable}>
              Añadir responsable
            </Button>
          </DialogActions>
        </DialogContent>
      </Dialog>

      {/* DIALOG AÑADIR/EDITAR RESPONSABLE */}
      <Dialog open={respAbierto} onClose={() => setRespAbierto(false)} fullWidth maxWidth="xs">
        <DialogTitle>{respEditar ? 'Editar responsable' : 'Nuevo responsable'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            <TextField
              label="Área / Oficina"
              fullWidth
              value={respForm.areaOficina}
              onChange={(e) => setRespForm((f) => ({ ...f, areaOficina: e.target.value }))}
            />
            <TextField
              label="Nombre y apellidos"
              fullWidth
              value={respForm.nombre}
              onChange={(e) => setRespForm((f) => ({ ...f, nombre: e.target.value }))}
            />
            <TextField
              label="Teléfono"
              fullWidth
              value={respForm.telefono}
              onChange={(e) => setRespForm((f) => ({ ...f, telefono: e.target.value }))}
            />
            <TextField
              label="E-mail"
              fullWidth
              value={respForm.email}
              onChange={(e) => setRespForm((f) => ({ ...f, email: e.target.value }))}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRespAbierto(false)}>Cancelar</Button>
          <Button
            variant="contained"
            disabled={guardarResponsable.isPending}
            onClick={() => guardarResponsable.mutate(respForm)}
          >
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
