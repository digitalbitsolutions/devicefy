import { Box, Container, Typography } from '@mui/material'

function App() {
  return (
    <Container maxWidth="md">
      <Box sx={{ mt: 8, textAlign: 'center' }}>
        <Typography variant="h3" component="h1" gutterBottom>
          Devicefy
        </Typography>
        <Typography variant="subtitle1" color="text.secondary">
          Inventario de equipos e intervenciones
        </Typography>
        <Typography variant="body2" sx={{ mt: 4 }}>
          Esqueleto de la aplicación. Las pantallas (login, dashboard, listados,
          fichas e intervenciones) se construirán en la fase 8.
        </Typography>
      </Box>
    </Container>
  )
}

export default App
