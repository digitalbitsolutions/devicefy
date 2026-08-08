import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import CentrosPage from './pages/CentrosPage'
import UbicacionesPage from './pages/UbicacionesPage'
import EquiposPage from './pages/EquiposPage'
import ImportacionPage from './pages/ImportacionPage'
import DesplieguesPage from './pages/DesplieguesPage'
import DespliegueDetallePage from './pages/DespliegueDetallePage'
import UsuariosPage from './pages/UsuariosPage'

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
        <Route path="/importacion" element={<ImportacionPage />} />
        <Route path="/despliegues" element={<DesplieguesPage />} />
        <Route path="/despliegues/:id" element={<DespliegueDetallePage />} />
        <Route path="/usuarios" element={<UsuariosPage />} />
        <Route path="/" element={<Navigate to="/centros" replace />} />
        <Route path="*" element={<Navigate to="/centros" replace />} />
      </Route>
    </Routes>
  )
}

export default App