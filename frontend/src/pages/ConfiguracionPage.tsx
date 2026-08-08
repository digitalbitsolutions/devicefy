import { Alert, Typography } from '@mui/material'

export default function ConfiguracionPage() {
  return (
    <>
      <Typography variant="h5" sx={{ fontWeight: 700, mb: 2 }}>
        Configuración
      </Typography>
      <Alert severity="info">La configuración del sistema estará disponible en una próxima versión.</Alert>
    </>
  )
}
