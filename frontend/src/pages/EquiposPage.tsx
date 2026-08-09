import { useEffect, useMemo, useState } from 'react'
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
  Grid,
  IconButton,
  MenuItem,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import { Add, Delete, Edit } from '@mui/icons-material'
import { useForm } from 'react-hook-form'
import {
  authApi,
  centrosApi,
  equiposApi,
  importacionesApi,
  ubicacionesApi,
  usuariosApi,
  type Equipo,
  type RedConfigRequest,
} from '../lib/api'
import { ESTADOS_EQUIPO, TIPOS_ASIGNACION_RED, TIPOS_EQUIPO } from '../lib/constants'

interface FormValues {
  hostname: string
  numeroSerie: string
  etiquetaPatrimonial: string
  fabricante: string
  modelo: string
  sistemaOperativo: string
  procesador: string
  tipoEquipo: string
  estado: string
  centroId: number
  ubicacionId: number
  observaciones: string
  activo: boolean
}

const redVacia = (): RedConfigRequest => ({
  tipoAsignacion: 'DHCP',
  ip: '',
  mascara: '',
  puertaEnlace: '',
  dns1: '',
  dns2: '',
  dominio: '',
})

function errorMessage(e: unknown): string {
  const data = (e as { response?: { data?: { error?: string } } }).response?.data
  return data?.error ?? 'Error al guardar'
}

export default function EquiposPage() {
  const queryClient = useQueryClient()
  const [filtro, setFiltro] = useState({
    hostname: '',
    estado: '',
    centroId: '',
    despliegueId: '',
    provincia: '',
    tecnicoId: '',
    activo: 'true',
  })
  const [open, setOpen] = useState(false)
  const [editId, setEditId] = useState<number | null>(null)
  const [red, setRed] = useState<RedConfigRequest>(redVacia())
  const [error, setError] = useState('')
  const [pagina, setPagina] = useState(0)
  const [registrosPorPagina, setRegistrosPorPagina] = useState(25)

  const { register, handleSubmit, reset, setValue, watch, formState: { errors } } =
    useForm<FormValues>({
      defaultValues: {
        hostname: '',
        numeroSerie: '',
        etiquetaPatrimonial: '',
        fabricante: '',
        modelo: '',
        sistemaOperativo: '',
        procesador: '',
        tipoEquipo: 'CPU',
        estado: 'PENDIENTE',
        centroId: 0,
        ubicacionId: 0,
        observaciones: '',
        activo: true,
      },
    })

  const centroSeleccionado = watch('centroId')

  const { data: centros = [] } = useQuery({ queryKey: ['centros'], queryFn: () => centrosApi.list() })

  const { data: despliegues = [] } = useQuery({
    queryKey: ['despliegues'],
    queryFn: importacionesApi.listarDespliegues,
  })
  const provincias = useMemo(
    () =>
      [
        ...new Set(
          despliegues
            .map((despliegue) => despliegue.provincia?.trim())
            .filter((provincia): provincia is string => Boolean(provincia)),
        ),
      ].sort((a, b) => a.localeCompare(b, 'es')),
    [despliegues],
  )

  const { data: authMe } = useQuery({ queryKey: ['authMe'], queryFn: authApi.me })
  const esAdmin = authMe?.authorities.includes('ROLE_ADMIN') ?? false
  const { data: usuarios = [] } = useQuery({
    queryKey: ['usuarios', 'tecnicos'],
    queryFn: usuariosApi.listar,
    enabled: esAdmin,
  })
  const tecnicos = usuarios.filter((usuario) => usuario.roles.includes('TECNICO'))
  const { data: ubicaciones = [] } = useQuery({
    queryKey: ['ubicaciones'],
    queryFn: () => ubicacionesApi.list(),
  })

  const ubicacionesDelCentro = useMemo(
    () => ubicaciones.filter((u) => u.centroId === centroSeleccionado),
    [ubicaciones, centroSeleccionado],
  )

  const { data: equipos = [], isLoading } = useQuery({
    queryKey: ['equipos', filtro],
    queryFn: () =>
      equiposApi.list({
        hostname: filtro.hostname || undefined,
        estado: filtro.estado || undefined,
        centroId: filtro.centroId ? Number(filtro.centroId) : undefined,
        despliegueId: filtro.despliegueId ? Number(filtro.despliegueId) : undefined,
        provincia: filtro.provincia || undefined,
        tecnicoId: filtro.tecnicoId ? Number(filtro.tecnicoId) : undefined,
        activo: filtro.activo === '' ? undefined : filtro.activo === 'true',
      }),
  })
  const equiposPaginados = useMemo(
    () => equipos.slice(pagina * registrosPorPagina, pagina * registrosPorPagina + registrosPorPagina),
    [equipos, pagina, registrosPorPagina],
  )

  useEffect(() => {
    setPagina(0)
  }, [filtro])

  useEffect(() => {
    const ultimaPagina = Math.max(0, Math.ceil(equipos.length / registrosPorPagina) - 1)
    setPagina((actual) => Math.min(actual, ultimaPagina))
  }, [equipos.length, registrosPorPagina])

  const guardar = useMutation({
    mutationFn: (values: FormValues) => {
      const body = {
        hostname: values.hostname,
        numeroSerie: values.numeroSerie,
        etiquetaPatrimonial: values.etiquetaPatrimonial,
        fabricante: values.fabricante,
        modelo: values.modelo,
        sistemaOperativo: values.sistemaOperativo,
        procesador: values.procesador,
        tipoEquipo: values.tipoEquipo,
        estado: values.estado,
        centroId: values.centroId || undefined,
        ubicacionId: values.ubicacionId || undefined,
        observaciones: values.observaciones,
        activo: values.activo,
        red:
          red.tipoAsignacion === 'DHCP'
            ? { tipoAsignacion: 'DHCP' as const }
            : {
                tipoAsignacion: 'ESTATICA' as const,
                ip: red.ip,
                mascara: red.mascara,
                puertaEnlace: red.puertaEnlace,
                dns1: red.dns1,
                dns2: red.dns2,
                dominio: red.dominio,
              },
      }
      return editId ? equiposApi.update(editId, body) : equiposApi.create(body)
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['equipos'] })
      setOpen(false)
      reset()
      setRed(redVacia())
      setEditId(null)
    },
    onError: (e) => setError(errorMessage(e)),
  })

  const eliminar = useMutation({
    mutationFn: (id: number) => equiposApi.remove(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['equipos'] }),
    onError: (e) => setError(errorMessage(e)),
  })

  const abrirCrear = () => {
    setError('')
    setEditId(null)
    reset({
      hostname: '',
      numeroSerie: '',
      etiquetaPatrimonial: '',
      fabricante: '',
      modelo: '',
      sistemaOperativo: '',
      procesador: '',
      tipoEquipo: 'CPU',
      estado: 'PENDIENTE',
      centroId: 0,
      ubicacionId: 0,
      observaciones: '',
      activo: true,
    })
    setRed(redVacia())
    setOpen(true)
  }

  const abrirEditar = (equipo: Equipo) => {
    setError('')
    setEditId(equipo.id)
    reset({
      hostname: equipo.hostname ?? '',
      numeroSerie: equipo.numeroSerie ?? '',
      etiquetaPatrimonial: equipo.etiquetaPatrimonial ?? '',
      fabricante: equipo.fabricante ?? '',
      modelo: equipo.modelo ?? '',
      sistemaOperativo: equipo.sistemaOperativo ?? '',
      procesador: equipo.procesador ?? '',
      tipoEquipo: equipo.tipoEquipo,
      estado: equipo.estado ?? 'PENDIENTE',
      centroId: equipo.centroId ?? 0,
      ubicacionId: equipo.ubicacionId ?? 0,
      observaciones: equipo.observaciones ?? '',
      activo: equipo.activo,
    })
    setRed({
      tipoAsignacion: equipo.red?.tipoAsignacion ?? 'DHCP',
      ip: equipo.red?.ip ?? '',
      mascara: equipo.red?.mascara ?? '',
      puertaEnlace: equipo.red?.puertaEnlace ?? '',
      dns1: equipo.red?.dns1 ?? '',
      dns2: equipo.red?.dns2 ?? '',
      dominio: equipo.red?.dominio ?? '',
    })
    setOpen(true)
  }

  const setRedField = (key: keyof RedConfigRequest) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setRed((prev) => ({ ...prev, [key]: e.target.value }))

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, gap: 2, flexWrap: 'wrap' }}>
        <Typography variant="h5" sx={{ flexGrow: 1 }}>
          Equipos
        </Typography>
        <TextField
          label="Hostname"
          size="small"
          value={filtro.hostname}
          onChange={(e) => setFiltro((f) => ({ ...f, hostname: e.target.value }))}
        />
        <TextField
          select
          label="Estado"
          size="small"
          value={filtro.estado}
          onChange={(e) => setFiltro((f) => ({ ...f, estado: e.target.value }))}
          sx={{ minWidth: 140 }}
        >
          <MenuItem value="">Todos</MenuItem>
          {ESTADOS_EQUIPO.map((e) => (
            <MenuItem key={e} value={e}>
              {e}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          label="Centro"
          size="small"
          value={filtro.centroId}
          onChange={(e) => setFiltro((f) => ({ ...f, centroId: e.target.value }))}
          sx={{ minWidth: 160 }}
        >
          <MenuItem value="">Todos</MenuItem>
          {centros.map((c) => (
            <MenuItem key={c.id} value={String(c.id)}>
              {c.nombre}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          label="Proyecto"
          size="small"
          value={filtro.despliegueId}
          onChange={(e) => setFiltro((f) => ({ ...f, despliegueId: e.target.value }))}
          sx={{ minWidth: 160 }}
        >
          <MenuItem value="">Todos</MenuItem>
          {despliegues.map((d) => (
            <MenuItem key={d.id} value={String(d.id)}>
              {d.nombre}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          label="Provincia"
          size="small"
          value={filtro.provincia}
          onChange={(e) => setFiltro((f) => ({ ...f, provincia: e.target.value }))}
          sx={{ minWidth: 160 }}
        >
          <MenuItem value="">Todas</MenuItem>
          {provincias.map((provincia) => (
            <MenuItem key={provincia} value={provincia}>
              {provincia}
            </MenuItem>
          ))}
        </TextField>
        {esAdmin && (
          <TextField
            select
            label="Técnico"
            size="small"
            value={filtro.tecnicoId}
            onChange={(e) => setFiltro((f) => ({ ...f, tecnicoId: e.target.value }))}
            sx={{ minWidth: 180 }}
          >
            <MenuItem value="">Todos</MenuItem>
            {tecnicos.map((tecnico) => (
              <MenuItem key={tecnico.id} value={String(tecnico.id)}>
                {tecnico.nombreCompleto}
                {!tecnico.activo ? ' (inactivo)' : ''}
              </MenuItem>
            ))}
          </TextField>
        )}
        <TextField
          select
          label="Activo"
          size="small"
          value={filtro.activo}
          onChange={(e) => setFiltro((f) => ({ ...f, activo: e.target.value }))}
          sx={{ minWidth: 110 }}
        >
          <MenuItem value="true">Sí</MenuItem>
          <MenuItem value="false">No</MenuItem>
          <MenuItem value="">Todos</MenuItem>
        </TextField>
        {esAdmin && (
          <Button variant="contained" startIcon={<Add />} onClick={abrirCrear}>
            Nuevo equipo
          </Button>
        )}
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      <Paper>
        <TableContainer>
          <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Hostname</TableCell>
              <TableCell>Tipo</TableCell>
              <TableCell>Estado</TableCell>
              <TableCell>Centro</TableCell>
              <TableCell>Ubicación</TableCell>
              <TableCell>Nº serie</TableCell>
              <TableCell>Activo</TableCell>
              {esAdmin && <TableCell align="right">Acciones</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {isLoading && (
              <TableRow>
                <TableCell colSpan={esAdmin ? 8 : 7} align="center">
                  Cargando…
                </TableCell>
              </TableRow>
            )}
            {!isLoading && equipos.length === 0 && (
              <TableRow>
                <TableCell colSpan={esAdmin ? 8 : 7} align="center">
                  Sin resultados
                </TableCell>
              </TableRow>
            )}
            {equiposPaginados.map((equipo) => (
              <TableRow key={equipo.id} hover>
                <TableCell>{equipo.hostname ?? '—'}</TableCell>
                <TableCell>{equipo.tipoEquipo}</TableCell>
                <TableCell>{equipo.estado ?? '—'}</TableCell>
                <TableCell>{equipo.centroNombre ?? '—'}</TableCell>
                <TableCell>{equipo.ubicacionNombre ?? '—'}</TableCell>
                <TableCell>{equipo.numeroSerie ?? '—'}</TableCell>
                <TableCell>{equipo.activo ? 'Sí' : 'No'}</TableCell>
                {esAdmin && (
                  <TableCell align="right">
                    <IconButton size="small" onClick={() => abrirEditar(equipo)}>
                      <Edit fontSize="small" />
                    </IconButton>
                    <IconButton size="small" color="error" onClick={() => eliminar.mutate(equipo.id)}>
                      <Delete fontSize="small" />
                    </IconButton>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          component="div"
          count={equipos.length}
          page={pagina}
          rowsPerPage={registrosPorPagina}
          rowsPerPageOptions={[10, 25, 50, 100]}
          onPageChange={(_, nuevaPagina) => setPagina(nuevaPagina)}
          onRowsPerPageChange={(e) => {
            setRegistrosPorPagina(Number(e.target.value))
            setPagina(0)
          }}
          labelRowsPerPage="Registros por página:"
          labelDisplayedRows={({ from, to, count }) => `${from}-${to} de ${count} registros`}
        />
      </Paper>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="md">
        <DialogTitle>{editId ? 'Editar equipo' : 'Nuevo equipo'}</DialogTitle>
        <DialogContent>
          <Box component="form" noValidate sx={{ mt: 1 }}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <TextField label="Hostname" fullWidth margin="normal" {...register('hostname')} />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField label="Nº serie" fullWidth margin="normal" {...register('numeroSerie')} />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField
                  label="Etiqueta patrimonial"
                  fullWidth
                  margin="normal"
                  {...register('etiquetaPatrimonial')}
                />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField label="Fabricante" fullWidth margin="normal" {...register('fabricante')} />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField label="Modelo" fullWidth margin="normal" {...register('modelo')} />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField
                  label="Sistema operativo"
                  fullWidth
                  margin="normal"
                  {...register('sistemaOperativo')}
                />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField label="Procesador" fullWidth margin="normal" {...register('procesador')} />
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField
                  select
                  label="Tipo de equipo"
                  fullWidth
                  margin="normal"
                  {...register('tipoEquipo', { required: 'El tipo es obligatorio' })}
                  error={!!errors.tipoEquipo}
                  helperText={errors.tipoEquipo?.message}
                >
                  {TIPOS_EQUIPO.map((t) => (
                    <MenuItem key={t} value={t}>
                      {t}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField
                  select
                  label="Estado"
                  fullWidth
                  margin="normal"
                  {...register('estado', { required: 'El estado es obligatorio' })}
                  error={!!errors.estado}
                  helperText={errors.estado?.message}
                >
                  {ESTADOS_EQUIPO.map((e) => (
                    <MenuItem key={e} value={e}>
                      {e}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField
                  select
                  label="Centro"
                  fullWidth
                  margin="normal"
                  {...register('centroId')}
                >
                  <MenuItem value={0}>Sin asignar</MenuItem>
                  {centros.map((c) => (
                    <MenuItem key={c.id} value={c.id}>
                      {c.nombre}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              <Grid size={{ xs: 6 }}>
                <TextField
                  select
                  label="Ubicación"
                  fullWidth
                  margin="normal"
                  {...register('ubicacionId')}
                >
                  <MenuItem value={0}>Sin asignar</MenuItem>
                  {ubicacionesDelCentro.map((u) => (
                    <MenuItem key={u.id} value={u.id}>
                      {u.nombre}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
                <Grid size={{ xs: 12 }}>
                <TextField
                  label="Observaciones"
                  fullWidth
                  margin="normal"
                  multiline
                  rows={2}
                  {...register('observaciones')}
                />
              </Grid>
            </Grid>

            <Typography variant="subtitle1" sx={{ mt: 2 }}>
              Configuración de red
            </Typography>
            <Grid container spacing={2}>
              <Grid size={{ xs: 6 }}>
                <TextField
                  select
                  label="Tipo de asignación"
                  fullWidth
                  margin="normal"
                  value={red.tipoAsignacion}
                  onChange={setRedField('tipoAsignacion')}
                >
                  {TIPOS_ASIGNACION_RED.map((t) => (
                    <MenuItem key={t} value={t}>
                      {t}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
              {red.tipoAsignacion === 'ESTATICA' && (
                <>
                  <Grid size={{ xs: 6 }}>
                    <TextField
                      label="IP"
                      fullWidth
                      margin="normal"
                      value={red.ip}
                      onChange={setRedField('ip')}
                    />
                  </Grid>
                  <Grid size={{ xs: 6 }}>
                    <TextField
                      label="Máscara"
                      fullWidth
                      margin="normal"
                      value={red.mascara}
                      onChange={setRedField('mascara')}
                    />
                  </Grid>
                  <Grid size={{ xs: 6 }}>
                    <TextField
                      label="Puerta de enlace"
                      fullWidth
                      margin="normal"
                      value={red.puertaEnlace}
                      onChange={setRedField('puertaEnlace')}
                    />
                  </Grid>
                  <Grid size={{ xs: 6 }}>
                    <TextField
                      label="DNS 1"
                      fullWidth
                      margin="normal"
                      value={red.dns1}
                      onChange={setRedField('dns1')}
                    />
                  </Grid>
                  <Grid size={{ xs: 6 }}>
                    <TextField
                      label="DNS 2"
                      fullWidth
                      margin="normal"
                      value={red.dns2}
                      onChange={setRedField('dns2')}
                    />
                  </Grid>
                  <Grid size={{ xs: 6 }}>
                    <TextField
                      label="Dominio"
                      fullWidth
                      margin="normal"
                      value={red.dominio}
                      onChange={setRedField('dominio')}
                    />
                  </Grid>
                </>
              )}
            </Grid>

            <FormControlLabel
              control={
                <Checkbox
                  defaultChecked
                  {...register('activo')}
                  onChange={(e) => setValue('activo', e.target.checked)}
                />
              }
              label="Activo"
              sx={{ mt: 1 }}
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
