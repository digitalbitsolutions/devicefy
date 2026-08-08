import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import CentrosPage from './pages/CentrosPage'
import UbicacionesPage from './pages/UbicacionesPage'
import EquiposPage from './pages/EquiposPage'
import ImportacionPage from './pages/ImportacionPage'
import DesplieguesPage from './pages/DesplieguesPage'
import DespliegueDetallePage from './pages/DespliegueDetallePage'
import UsuariosPage from './pages/UsuariosPage'
import InformesPage from './pages/InformesPage'
import ConfiguracionPage from './pages/ConfiguracionPage'

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
        <Route path="/" element={<DashboardPage />} />
        <Route path="/equipos" element={<EquiposPage />} />
        <Route path="/centros" element={<CentrosPage />} />
        <Route path="/ubicaciones" element={<UbicacionesPage />} />
        <Route path="/importacion" element={<ImportacionPage />} />
        <Route path="/despliegues" element={<DesplieguesPage />} />
        <Route path="/despliegues/:id" element={<DespliegueDetallePage />} />
        <Route path="/usuarios" element={<UsuariosPage />} />
        <Route path="/informes" element={<InformesPage />} />
        <Route path="/configuracion" element={<ConfiguracionPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}

export default App
