import { useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import {
  Avatar,
  Badge,
  Box,
  Button,
  Divider,
  IconButton,
  InputAdornment,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import {
  Assignment,
  ChevronLeft,
  ChevronRight,
  Dashboard,
  Dns,
  ExpandMore,
  FileCopy,
  HomeWork,
  Monitor,
  Notifications,
  Person,
  Search,
  Settings,
  Upload,
} from '@mui/icons-material'
import { clearToken } from '../lib/api'
import { useAuth } from '../hooks/useAuth'

interface ItemNav {
  label: string
  ruta: string
  icono: React.ReactNode
}

export default function Layout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { esAdmin, username } = useAuth()
  const [colapsado, setColapsado] = useState(false)

  const esActivo = (ruta: string) =>
    ruta === '/'
      ? location.pathname === '/'
      : location.pathname.startsWith(ruta)

  const logout = () => {
    clearToken()
    navigate('/login', { replace: true })
  }

  const principales: ItemNav[] = [
    { label: 'Dashboard', ruta: '/', icono: <Dashboard /> },
    { label: 'Equipos', ruta: '/equipos', icono: <Dns /> },
    { label: 'Periféricos', ruta: '/equipos?filtro=perifericos', icono: <Monitor /> },
    { label: 'Centros', ruta: '/centros', icono: <HomeWork /> },
    { label: 'Intervenciones', ruta: '/equipos', icono: <Assignment /> },
    { label: 'Importar Excel', ruta: '/importacion', icono: <Upload /> },
    { label: 'Informes', ruta: '/informes', icono: <FileCopy /> },
  ]

  const admin: ItemNav[] = [
    { label: 'Usuarios', ruta: '/usuarios', icono: <Person /> },
    { label: 'Configuración', ruta: '/configuracion', icono: <Settings /> },
  ]

  const ancho = colapsado ? 76 : 250

  const renderItem = (item: ItemNav) => {
    const activo = esActivo(item.ruta)
    const onClick = () => {
      if (item.ruta.includes('?')) {
        const [path, query] = item.ruta.split('?')
        navigate({ pathname: path, search: query })
      } else {
        navigate(item.ruta)
      }
    }
    return (
      <ListItemButton
        key={item.label}
        selected={activo}
        onClick={onClick}
        sx={{
          borderRadius: 2,
          mb: 0.5,
          mx: 1,
          minHeight: 44,
          px: colapsado ? 1.5 : 1.5,
          '&.Mui-selected': {
            backgroundColor: 'primary.main',
            color: '#fff',
            '&:hover': { backgroundColor: 'primary.main' },
            '& .MuiListItemIcon-root': { color: '#fff' },
          },
        }}
      >
        <Tooltip title={colapsado ? item.label : ''} placement="right">
          <ListItemIcon sx={{ minWidth: 40, color: 'text.secondary' }}>{item.icono}</ListItemIcon>
        </Tooltip>
        {!colapsado && <ListItemText primary={item.label} />}
      </ListItemButton>
    )
  }

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* SIDEBAR */}
      <Box
        component="aside"
        sx={{
          width: ancho,
          flexShrink: 0,
          bgcolor: 'background.paper',
          borderRight: '1px solid #eef2f7',
          display: 'flex',
          flexDirection: 'column',
          position: 'sticky',
          top: 0,
          height: '100vh',
          transition: 'width 0.18s ease',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 2, py: 2 }}>
          <Box
            sx={{
              width: 36,
              height: 36,
              borderRadius: 2,
              bgcolor: 'primary.main',
              color: '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontWeight: 700,
              fontSize: 18,
              flexShrink: 0,
            }}
          >
            D
          </Box>
          {!colapsado && (
            <Typography variant="h6" sx={{ fontWeight: 700, color: 'text.primary' }}>
              Devicefy
            </Typography>
          )}
        </Box>

        <Box component="nav" sx={{ flex: 1, overflowY: 'auto', py: 1 }}>
          <List disablePadding>{principales.map(renderItem)}</List>
          {!colapsado && (
            <Typography
              variant="caption"
              sx={{ px: 2.5, pt: 2, pb: 1, display: 'block', color: 'text.disabled', fontWeight: 600 }}
            >
              Administración
            </Typography>
          )}
          {colapsado && <Divider sx={{ mt: 1, mb: 1 }} />}
          <List disablePadding>{admin.map(renderItem)}</List>
        </Box>

        <Box sx={{ borderTop: '1px solid #eef2f7', p: 1 }}>
          <ListItemButton
            onClick={() => setColapsado((c) => !c)}
            sx={{ borderRadius: 2, minHeight: 40, mx: 0.5 }}
          >
            <ListItemIcon sx={{ minWidth: 40, color: 'text.secondary' }}>
              {colapsado ? <ChevronRight /> : <ChevronLeft />}
            </ListItemIcon>
            {!colapsado && <ListItemText primary="Contraer" />}
          </ListItemButton>
          {!colapsado && (
            <Typography variant="caption" sx={{ px: 2, display: 'block', color: 'text.disabled' }}>
              Devicefy v1.0.0
            </Typography>
          )}
        </Box>
      </Box>

      {/* CONTENIDO */}
      <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        <Box
          component="header"
          sx={{
            bgcolor: 'background.paper',
            borderBottom: '1px solid #eef2f7',
            px: 3,
            py: 1.25,
            display: 'flex',
            alignItems: 'center',
            gap: 2,
          }}
        >
          <Typography variant="h6" sx={{ fontWeight: 700, flex: 1 }}>
            {esActivo('/') ? 'Dashboard' : esActivo('/equipos') ? 'Equipos' : 'Devicefy'}
          </Typography>
          <TextField
            placeholder="Buscar equipo, serie o usuario..."
            size="small"
            sx={{ width: 300, '& fieldset': { borderColor: '#e2e8f0' } }}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <Search fontSize="small" />
                  </InputAdornment>
                ),
              },
            }}
          />
          <IconButton size="small">
            <Badge badgeContent={3} color="error" variant="dot">
              <Notifications />
            </Badge>
          </IconButton>
          <Stack direction="row" spacing={1} sx={{ ml: 1, alignItems: 'center' }}>
            <Avatar sx={{ bgcolor: 'primary.main', width: 34, height: 34 }}>
              {username.charAt(0).toUpperCase()}
            </Avatar>
            <Box sx={{ display: { xs: 'none', md: 'block' } }}>
              <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.1 }}>
                {esAdmin ? 'Administrador' : username}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {username}
              </Typography>
            </Box>
            <ExpandMore fontSize="small" color="disabled" />
          </Stack>
          <Button color="inherit" onClick={logout} sx={{ ml: 1, textTransform: 'none' }}>
            Salir
          </Button>
        </Box>

        <Box component="main" sx={{ flex: 1, p: 3, overflow: 'auto' }}>
          <Outlet />
        </Box>

        <Box component="footer" sx={{ py: 2, textAlign: 'center' }}>
          <Typography variant="caption" color="text.disabled">
            © 2025 Devicefy. Todos los derechos reservados.
          </Typography>
        </Box>
      </Box>
    </Box>
  )
}
