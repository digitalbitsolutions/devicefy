import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Alert,
  Box,
  Button,
  Chip,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import { CloudUpload } from '@mui/icons-material'
import { importacionesApi, type ImportacionResult } from '../lib/api'

export default function ImportacionPage() {
  const navigate = useNavigate()
  const [nombre, setNombre] = useState('')
  const [archivo, setArchivo] = useState<File | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState('')
  const [resultado, setResultado] = useState<ImportacionResult | null>(null)

  const submit = async () => {
    setError('')
    if (!nombre.trim()) {
      setError('Indica el nombre del despliegue')
      return
    }
    if (!archivo) {
      setError('Selecciona un archivo Excel (.xlsx)')
      return
    }
    setEnviando(true)
    try {
      const r = await importacionesApi.importar(nombre.trim(), archivo)
      setResultado(r)
    } catch (e) {
      const data = (e as { response?: { data?: { error?: string } } }).response?.data
      setError(data?.error ?? 'Error al importar')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2 }}>
        Importación de Excel
      </Typography>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Stack spacing={2}>
          <TextField
            label="Nombre del despliegue"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
            placeholder="Ej. Tarragona, Terres de l'Ebre, Girona"
          />
          <Button component="label" variant="outlined" startIcon={<CloudUpload />} sx={{ alignSelf: 'flex-start' }}>
            {archivo ? archivo.name : 'Seleccionar archivo Excel'}
            <input type="file" accept=".xlsx" hidden onChange={(e) => setArchivo(e.target.files?.[0] ?? null)} />
          </Button>
          {error && <Alert severity="error">{error}</Alert>}
          <Button variant="contained" onClick={submit} disabled={enviando} sx={{ alignSelf: 'flex-start' }}>
            {enviando ? 'Importando…' : 'Importar'}
          </Button>
        </Stack>
      </Paper>

      {resultado && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Informe de importación
          </Typography>
          <Stack direction="row" spacing={1} sx={{ mb: 2, flexWrap: 'wrap' }}>
            <Chip label={`Filas leídas: ${resultado.filasLeidas}`} />
            <Chip label={`Equipos creados: ${resultado.equiposCreados}`} color="success" />
            <Chip label={`Centros: ${resultado.centrosCreados}`} />
            <Chip label={`Ubicaciones: ${resultado.ubicacionesCreadas}`} />
            {resultado.errores > 0 && <Chip label={`Errores: ${resultado.errores}`} color="error" />}
          </Stack>
          {resultado.errores > 0 ? (
            <>
              <Alert severity="warning" sx={{ mb: 2 }}>
                {resultado.errores} filas no se importaron:
              </Alert>
              <TableContainer component={Paper} variant="outlined">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Fila</TableCell>
                      <TableCell>Motivo</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {resultado.erroresDetalle.map((e) => (
                      <TableRow key={e.fila}>
                        <TableCell>{e.fila}</TableCell>
                        <TableCell>{e.motivo}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </>
          ) : (
            <Alert severity="success">Importación completada sin errores.</Alert>
          )}
          <Button sx={{ mt: 2 }} onClick={() => navigate(`/despliegues/${resultado.despliegueId}`)}>
            Ver equipos del despliegue
          </Button>
        </Paper>
      )}
    </Box>
  )
}