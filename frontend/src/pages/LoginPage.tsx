import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { Alert, Box, Button, Container, Paper, TextField, Typography } from '@mui/material'
import { useForm } from 'react-hook-form'
import { authApi, getToken, setToken } from '../lib/api'

interface FormValues {
  username: string
  password: string
}

export default function LoginPage() {
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const { register, handleSubmit, formState: { errors } } = useForm<FormValues>()

  if (getToken()) {
    return <Navigate to="/centros" replace />
  }

  const onSubmit = async (data: FormValues) => {
    setError('')
    try {
      const res = await authApi.login(data)
      setToken(res.token)
      navigate('/centros', { replace: true })
    } catch (e) {
      setError((e as { response?: { data?: { error?: string } } }).response?.data?.error ?? 'Error al iniciar sesión')
    }
  }

  return (
    <Container maxWidth="xs">
      <Paper sx={{ mt: 10, p: 4 }}>
        <Typography variant="h5" component="h1" gutterBottom align="center">
          Devicefy
        </Typography>
        <Typography variant="subtitle2" align="center" color="text.secondary" sx={{ mb: 3 }}>
          Inventario de equipos e intervenciones
        </Typography>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
          <TextField
            label="Usuario"
            fullWidth
            margin="normal"
            autoFocus
            {...register('username', { required: 'El usuario es obligatorio' })}
            error={!!errors.username}
            helperText={errors.username?.message}
          />
          <TextField
            label="Contraseña"
            type="password"
            fullWidth
            margin="normal"
            {...register('password', { required: 'La contraseña es obligatoria' })}
            error={!!errors.password}
            helperText={errors.password?.message}
          />
          <Button type="submit" variant="contained" fullWidth size="large" sx={{ mt: 3 }}>
            Entrar
          </Button>
        </Box>
      </Paper>
    </Container>
  )
}
