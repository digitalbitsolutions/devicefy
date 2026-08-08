import { useNavigate } from 'react-router-dom'
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
import { ArrowForward } from '@mui/icons-material'
import { useQuery } from '@tanstack/react-query'
import { importacionesApi } from '../lib/api'

const chipColor = (estado: string) => {
  if (estado === 'HECHO') return 'success'
  if (estado === 'EN_PROCESO' || estado === 'PROCESANDO') return 'primary'
  return 'default'
}

export default function DesplieguesPage() {
  const navigate = useNavigate()
  const { data: despliegues = [], isLoading } = useQuery({
    queryKey: ['despliegues'],
    queryFn: importacionesApi.listarDespliegues,
  })

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2 }}>
        Despliegues
      </Typography>
      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Nombre</TableCell>
              <TableCell>Fichero</TableCell>
              <TableCell>Importado</TableCell>
              <TableCell align="center">Total</TableCell>
              <TableCell align="center">En proceso</TableCell>
              <TableCell align="center">Hechos</TableCell>
              <TableCell>Estado</TableCell>
              <TableCell align="right" />
            </TableRow>
          </TableHead>
          <TableBody>
            {despliegues.map((d) => (
              <TableRow key={d.id} hover>
                <TableCell>{d.nombre}</TableCell>
                <TableCell>{d.ficheroNombre}</TableCell>
                <TableCell>{d.fechaImportacion ? new Date(d.fechaImportacion).toLocaleString() : ''}</TableCell>
                <TableCell align="center">{d.totalEquipos}</TableCell>
                <TableCell align="center">{d.enProceso}</TableCell>
                <TableCell align="center">{d.hechos}</TableCell>
                <TableCell>
                  <Chip size="small" label={d.estado} color={chipColor(d.estado)} />
                </TableCell>
                <TableCell align="right">
                  <IconButton size="small" onClick={() => navigate(`/despliegues/${d.id}`)}>
                    <ArrowForward />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
            {!isLoading && despliegues.length === 0 && (
              <TableRow>
                <TableCell colSpan={8} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                  No hay despliegues. Importa un Excel desde la pestaña Importación.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  )
}