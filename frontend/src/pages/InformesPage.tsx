import { Alert, Typography } from '@mui/material'

export default function InformesPage() {
  return (
    <>
      <Typography variant="h5" sx={{ fontWeight: 700, mb: 2 }}>
        Informes
      </Typography>
      <Alert severity="info">Los informes estarán disponibles en una próxima versión.</Alert>
    </>
  )
}
