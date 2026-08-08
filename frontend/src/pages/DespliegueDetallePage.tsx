import { useNavigate, useParams } from 'react-router-dom'
import {
  Box,
  Chip,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import { ArrowBack } from '@mui/icons-material'
import { useQuery } from '@tanstack/react-query'
import { importacionesApi } from '../lib/api'

const chipColor = (estado: string) => {
  if (estado === 'HECHO') return 'success'
  if (estado === 'EN_PROCESO' || estado === 'PROCESANDO') return 'primary'
  return 'default'
}

export default function DespliegueDetallePage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const despliegueId = Number(id)
  const { data: equipos = [], isLoading } = useQuery({
    queryKey: ['despliegue-equipos', despliegueId],
    queryFn: () => importacionesApi.listarEquipos(despliegueId),
  })

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
        <IconButton onClick={() => navigate('/despliegues')}>
          <ArrowBack />
        </IconButton>
        <Typography variant="h5">
          Equipos del despliegue
        </Typography>
      </Box>
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Hostname</TableCell>
              <TableCell>Serie</TableCell>
              <TableCell>Fabricante / Modelo</TableCell>
              <TableCell>Centro</TableCell>
              <TableCell>Ubicación</TableCell>
              <TableCell align="center">Renove</TableCell>
              <TableCell align="center">Año</TableCell>
              <TableCell>Perfil</TableCell>
              <TableCell>IP</TableCell>
              <TableCell>Técnico</TableCell>
              <TableCell>Estado</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {equipos.map((e) => (
              <TableRow key={e.id} hover>
                <TableCell>{e.hostnameActual}</TableCell>
                <TableCell>{e.numeroSerie}</TableCell>
                <TableCell>
                  {e.fabricante} {e.modelo}
                </TableCell>
                <TableCell>{e.centroNombre}</TableCell>
                <TableCell>{e.ubicacionNombre}</TableCell>
                <TableCell align="center">{e.estadoRenove ?? ''}</TableCell>
                <TableCell align="center">{e.anioRenove ?? ''}</TableCell>
                <TableCell>{e.perfilImagen}</TableCell>
                <TableCell>{e.ip}</TableCell>
                <TableCell>{e.tecnicoNombre}</TableCell>
                <TableCell>
                  <Chip size="small" label={e.estado} color={chipColor(e.estado)} />
                </TableCell>
              </TableRow>
            ))}
            {!isLoading && equipos.length === 0 && (
              <TableRow>
                <TableCell colSpan={11} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No hay equipos en este despliegue.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  )
}