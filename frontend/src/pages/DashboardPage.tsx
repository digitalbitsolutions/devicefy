import { useQuery } from '@tanstack/react-query'
import { Link as RouterLink } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import {
  Build,
  CheckCircle,
  Computer,
  Monitor,
  Pending,
  Upload,
} from '@mui/icons-material'
import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip as ChartTooltip,
  Bar,
  BarChart,
  CartesianGrid,
  XAxis,
  YAxis,
} from 'recharts'
import { dashboardApi, equiposApi } from '../lib/api'

const COLOR_ESTADO: Record<string, string> = {
  PENDIENTE: '#f59e0b',
  EN_PROCESO: '#0ea5e9',
  FINALIZADO: '#16a34a',
  BAJA: '#94a3b8',
}

function chipColor(estado: string): 'warning' | 'info' | 'success' | 'default' {
  switch (estado) {
    case 'PENDIENTE':
      return 'warning'
    case 'EN_PROCESO':
      return 'info'
    case 'FINALIZADO':
      return 'success'
    default:
      return 'default'
  }
}

function KpiCard({
  titulo,
  valor,
  icono,
  color,
  texto,
}: {
  titulo: string
  valor: number
  icono: React.ReactNode
  color: string
  texto: string
}) {
  return (
    <Card>
      <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Box
          sx={{
            width: 46,
            height: 46,
            borderRadius: 2,
            bgcolor: `${color}15`,
            color,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
          }}
        >
          {icono}
        </Box>
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="h4" sx={{ fontWeight: 700, lineHeight: 1.1 }}>
            {valor.toLocaleString('es-ES')}
          </Typography>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {titulo}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {texto}
          </Typography>
        </Box>
      </CardContent>
    </Card>
  )
}

export default function DashboardPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['dashboard'],
    queryFn: dashboardApi.resumen,
  })

  const { data: equipos = [] } = useQuery({
    queryKey: ['equipos', { dashboard: true }],
    queryFn: () => equiposApi.list({}),
  })

  if (isError) {
    return (
      <Alert severity="error">
        No se pudieron cargar los datos del dashboard. Asegúrate de tener permisos de administrador.
      </Alert>
    )
  }

  if (isLoading || !data) {
    return <Typography color="text.secondary">Cargando dashboard…</Typography>
  }

  const { kpis, equiposPorEstado, progresoProyectos, cargaTecnicos } = data

  const estadosDonut = ['PENDIENTE', 'EN_PROCESO', 'FINALIZADO', 'BAJA']
    .map((e) => {
      const fila = equiposPorEstado.find((x) => x.nombre === e)
      return { name: e, value: fila?.valor ?? 0 }
    })
    .filter((e) => e.value > 0)

  const totalDonut = estadosDonut.reduce((a, b) => a + b.value, 0)

  const equiposAtencion = equipos
    .filter((e) => e.estado === 'PENDIENTE' || e.estado === 'EN_PROCESO')
    .slice(0, 5)

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Resumen general
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Estado actual del inventario y las intervenciones.
        </Typography>
      </Box>

      {/* PRIMERA FILA: KPI CARDS */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <KpiCard
            titulo="Equipos"
            valor={kpis.totalEquipos}
            icono={<Computer />}
            color="#1976d2"
            texto="Equipos registrados en el inventario"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <KpiCard
            titulo="Pendientes"
            valor={kpis.equiposPendientes}
            icono={<Pending />}
            color="#f59e0b"
            texto="Equipos pendientes de procesar"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <KpiCard
            titulo="En proceso"
            valor={kpis.equiposEnProceso}
            icono={<Build />}
            color="#0ea5e9"
            texto="Equipos en curso por técnicos"
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <KpiCard
            titulo="Finalizados"
            valor={kpis.equiposFinalizados}
            icono={<CheckCircle />}
            color="#16a34a"
            texto="Equipos finalizados"
          />
        </Grid>
      </Grid>

      {/* SEGUNDA FILA: ESTADO DE EQUIPOS + PROGRESO PROYECTOS */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 5 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  Estado de equipos
                </Typography>
              </Box>
              <Box sx={{ height: 240, position: 'relative' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={estadosDonut}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      innerRadius={68}
                      outerRadius={90}
                      paddingAngle={2}
                      strokeWidth={0}
                      labelLine={false}
                      label={(p: { cx: number; cy: number; index: number }) =>
                        p.index === 0 ? (
                          <g>
                            <text
                              x={p.cx}
                              y={p.cy}
                              textAnchor="middle"
                              dominantBaseline="central"
                              fill="#0f172a"
                              style={{ fontSize: 26, fontWeight: 700, fontFamily: 'Inter, sans-serif' }}
                            >
                              {totalDonut.toLocaleString('es-ES')}
                            </text>
                            <text
                              x={p.cx}
                              y={p.cy + 18}
                              textAnchor="middle"
                              dominantBaseline="central"
                              fill="#94a3b8"
                              style={{ fontSize: 12, fontFamily: 'Inter, sans-serif' }}
                            >
                              Total
                            </text>
                          </g>
                        ) : null
                      }
                    >
                      {estadosDonut.map((e) => (
                        <Cell key={e.name} fill={COLOR_ESTADO[e.name] ?? '#94a3b8'} />
                      ))}
                    </Pie>
                    <ChartTooltip />
                    <Legend
                      verticalAlign="middle"
                      align="right"
                      layout="vertical"
                      formatter={(value: string) => (
                        <span style={{ fontSize: 13, color: '#475569' }}>
                          {value}
                          {estadosDonut.find((x) => x.name === value)
                            ? `  ${((estadosDonut.find((x) => x.name === value)!.value / totalDonut) * 100).toFixed(1)}%`
                            : ''}
                        </span>
                      )}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </Box>
              <Button
                component={RouterLink}
                to="/equipos"
                size="small"
                sx={{ mt: 1, textTransform: 'none' }}
              >
                Ver todos los equipos &gt;
              </Button>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 7 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 1 }}>
                Progreso de proyectos
              </Typography>
              {progresoProyectos.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                  No hay proyectos importados todavía.
                </Typography>
              ) : (
                <Box sx={{ height: 240 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={progresoProyectos} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                      <XAxis dataKey="nombre" tick={{ fontSize: 12 }} interval={0} angle={-12} textAnchor="end" height={46} />
                      <YAxis tick={{ fontSize: 12 }} allowDecimals={false} />
                      <ChartTooltip />
                      <Legend wrapperStyle={{ fontSize: 12 }} />
                      <Bar dataKey="hechos" name="Finalizados" stackId="a" fill="#16a34a" radius={[0, 0, 0, 0]} />
                      <Bar dataKey="enProceso" name="En proceso" stackId="a" fill="#0ea5e9" />
                      <Bar dataKey="total" name="Total" fill="#cbd5e1" />
                    </BarChart>
                  </ResponsiveContainer>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* TERCERA FILA: EQUIPOS QUE REQUIEREN ATENCIÓN + ACCIONES */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 7 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 700, mb: 1 }}>
                Equipos que requieren atención
              </Typography>
              <TableContainer component={Paper} variant="outlined" sx={{ border: 'none' }}>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Hostname</TableCell>
                      <TableCell>Centro</TableCell>
                      <TableCell>IP</TableCell>
                      <TableCell>Estado</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {equiposAtencion.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={4} align="center">
                          <Typography variant="body2" color="text.secondary">
                            No hay equipos pendientes.
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ) : (
                      equiposAtencion.map((e) => (
                        <TableRow key={e.id} hover>
                          <TableCell sx={{ fontWeight: 600 }}>{e.hostname}</TableCell>
                          <TableCell>{e.centroNombre ?? '—'}</TableCell>
                          <TableCell>{e.red?.ip ?? '—'}</TableCell>
                          <TableCell>
                            <Chip
                              label={e.estado ?? '—'}
                              size="small"
                              color={chipColor(e.estado ?? '')}
                              variant="outlined"
                            />
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
              <Button
                component={RouterLink}
                to="/equipos"
                size="small"
                sx={{ mt: 1, float: 'right', textTransform: 'none' }}
              >
                Ver todos &gt;
              </Button>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 5 }}>
          <Stack spacing={2}>
            <Card>
              <CardContent>
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 1.5 }}>
                  Acciones rápidas
                </Typography>
                <Stack spacing={1}>
                  <Button
                    component={RouterLink}
                    to="/equipos"
                    variant="outlined"
                    color="primary"
                    startIcon={<Computer />}
                    sx={{ textTransform: 'none', justifyContent: 'flex-start' }}
                  >
                    Nuevo equipo
                  </Button>
                  <Button
                    component={RouterLink}
                    to="/equipos"
                    variant="outlined"
                    color="primary"
                    startIcon={<Build />}
                    sx={{ textTransform: 'none', justifyContent: 'flex-start' }}
                  >
                    Nueva intervención
                  </Button>
                  <Button
                    component={RouterLink}
                    to="/importacion"
                    variant="outlined"
                    color="primary"
                    startIcon={<Upload />}
                    sx={{ textTransform: 'none', justifyContent: 'flex-start' }}
                  >
                    Importar Excel
                  </Button>
                </Stack>
              </CardContent>
            </Card>

            <Card>
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box
                  sx={{
                    width: 46,
                    height: 46,
                    borderRadius: 2,
                    bgcolor: '#1976d215',
                    color: '#1976d2',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Monitor />
                </Box>
                <Box sx={{ flex: 1 }}>
                  <Typography variant="h4" sx={{ fontWeight: 700, lineHeight: 1.1 }}>
                    —
                  </Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    Periféricos
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Periféricos registrados
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Stack>
        </Grid>
      </Grid>

      {/* CARGA POR TÉCNICO */}
      <Card>
        <CardContent>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 1 }}>
            Carga por técnico
          </Typography>
          {cargaTecnicos.length === 0 ? (
            <Typography variant="body2" color="text.secondary">
              Sin equipos asignados a técnicos.
            </Typography>
          ) : (
            <TableContainer component={Paper} variant="outlined" sx={{ border: 'none' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Técnico</TableCell>
                    <TableCell>Asignados</TableCell>
                    <TableCell>Finalizados</TableCell>
                    <TableCell>Progreso</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {cargaTecnicos.map((t) => {
                    const pct =
                      t.asignados > 0 ? Math.round((t.finalizados / t.asignados) * 100) : 0
                    return (
                      <TableRow key={t.id} hover>
                        <TableCell sx={{ fontWeight: 600 }}>{t.nombre}</TableCell>
                        <TableCell>{t.asignados}</TableCell>
                        <TableCell>{t.finalizados}</TableCell>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Box
                              sx={{
                                width: 120,
                                height: 8,
                                borderRadius: 4,
                                bgcolor: '#eef2f7',
                                overflow: 'hidden',
                              }}
                            >
                              <Box
                                sx={{
                                  width: `${pct}%`,
                                  height: '100%',
                                  bgcolor: '#16a34a',
                                  borderRadius: 4,
                                }}
                              />
                            </Box>
                            <Typography variant="caption" color="text.secondary">
                              {pct}%
                            </Typography>
                          </Box>
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </CardContent>
      </Card>
    </Box>
  )
}
