import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { AppBar, Box, Button, Container, Tab, Tabs, Toolbar, Typography } from '@mui/material'
import { clearToken } from '../lib/api'

export default function Layout() {
  const navigate = useNavigate()
  const location = useLocation()

  const current = location.pathname.startsWith('/ubicaciones')
    ? '/ubicaciones'
    : location.pathname.startsWith('/equipos')
      ? '/equipos'
      : '/centros'

  const logout = () => {
    clearToken()
    navigate('/login', { replace: true })
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 700 }}>
            Devicefy
          </Typography>
          <Tabs
            value={current}
            onChange={(_, value) => navigate(value)}
            textColor="inherit"
            indicatorColor="secondary"
          >
            <Tab value="/centros" label="Centros" />
            <Tab value="/ubicaciones" label="Ubicaciones" />
            <Tab value="/equipos" label="Equipos" />
          </Tabs>
          <Button color="inherit" onClick={logout}>
            Salir
          </Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ mt: 3, pb: 5, flex: 1 }}>
        <Outlet />
      </Container>
    </Box>
  )
}
