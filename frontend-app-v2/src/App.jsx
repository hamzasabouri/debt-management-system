import React from 'react'
import { Routes, Route } from 'react-router-dom'
import { ThemeProvider, createTheme } from '@mui/material/styles'
import CssBaseline from '@mui/material/CssBaseline'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import Dashboard from './components/Dashboard'
import DetteDuTresorDashboard from './components/dette-du-tresor/Dashboard'
import MEDADashboard from './components/meda-service/Dashboard'
import DetteInterieurDashboard from './components/dette-interieur/Dashboard'
import AdminDashboard from './components/admin/Dashboard'
// Import individual components for dette-interieur
import AdjudicationsList from './components/dette-interieur/components/AdjudicationsList'
import BonEquipementList from './components/dette-interieur/components/BonEquipementList'
import AvisBonEquipementList from './components/dette-interieur/components/AvisBonEquipementList'
import CommissionList from './components/dette-interieur/components/CommissionList'
import InteretDepotList from './components/dette-interieur/components/InteretDepotList'
import NotificationList from './components/dette-interieur/components/NotificationList'
import AvisAdjudicationList from './components/dette-interieur/components/AvisAdjudicationList'
import './App.css'

// Création du thème personnalisé
const theme = createTheme({
  palette: {
    primary: {
      main: '#FF6B35', // Orange vif
      light: '#FF8C5A', // Orange clair
      dark: '#C74A00', // Orange foncé
      contrastText: '#fff',
    },
    secondary: {
      main: '#2D3748', // Gris foncé
      light: '#4A5568', // Gris moyen
      dark: '#1A202C', // Gris très foncé
      contrastText: '#fff',
    },
    background: {
      default: '#F7FAFC', // Gris très clair
      paper: '#FFFFFF',   // Blanc
    },
    text: {
      primary: '#2D3748', // Gris foncé
      secondary: '#4A5568', // Gris moyen
    },
  },
  typography: {
    fontFamily: '"Inter", "Helvetica", "Arial", sans-serif',
    h1: {
      fontWeight: 700,
      fontSize: '2.5rem',
    },
    h2: {
      fontWeight: 600,
      fontSize: '2rem',
    },
    h3: {
      fontWeight: 600,
      fontSize: '1.5rem',
    },
    button: {
      textTransform: 'none',
      fontWeight: 500,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          padding: '10px 24px',
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0 4px 12px rgba(255, 107, 53, 0.3)',
          },
        },
        contained: {
          '&:hover': {
            backgroundColor: '#E65A2B',
          },
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 8,
            '& fieldset': {
              borderColor: '#E2E8F0',
            },
            '&:hover fieldset': {
              borderColor: '#CBD5E0',
            },
            '&.Mui-focused fieldset': {
              borderColor: '#FF6B35',
              borderWidth: '1px',
            },
          },
          '& .MuiInputLabel-root.Mui-focused': {
            color: '#FF6B35',
          },
        },
      },
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <div className="App">
          <Routes>
            <Route path="/" element={<LoginPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/dashboard" element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } />
            <Route path="/admin/*" element={
              <ProtectedRoute requiredRoles={['admin']}>
                <AdminDashboard />
              </ProtectedRoute>
            } />
            <Route path="/dette-du-tresor/*" element={
              <ProtectedRoute requiredRoles={['dette du tresor']}>
                <DetteDuTresorDashboard />
              </ProtectedRoute>
            } />
            <Route path="/meda/*" element={
              <ProtectedRoute requiredRoles={['MEDA']}>
                <MEDADashboard />
              </ProtectedRoute>
            } />
            <Route path="/dette-interieur/*" element={
              <ProtectedRoute requiredRoles={['dette interieur']}>
                <DetteInterieurDashboard />
              </ProtectedRoute>
            }>
              <Route path="adjudications" element={<AdjudicationsList />} />
              <Route path="bon-equipement" element={<BonEquipementList />} />
              <Route path="avis-bon-equipement" element={<AvisBonEquipementList />} />
              <Route path="commissions" element={<CommissionList />} />
              <Route path="interet-depot" element={<InteretDepotList />} />
              <Route path="notifications" element={<NotificationList />} />
              <Route path="avis-adjudication" element={<AvisAdjudicationList />} />
            </Route>
          </Routes>
        </div>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App