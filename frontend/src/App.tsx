import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import CentrosPage from './pages/CentrosPage'
import UbicacionesPage from './pages/UbicacionesPage'
import EquiposPage from './pages/EquiposPage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/centros" element={<CentrosPage />} />
        <Route path="/ubicaciones" element={<UbicacionesPage />} />
        <Route path="/equipos" element={<EquiposPage />} />
        <Route path="/" element={<Navigate to="/centros" replace />} />
        <Route path="*" element={<Navigate to="/centros" replace />} />
      </Route>
    </Routes>
  )
}

export default App
