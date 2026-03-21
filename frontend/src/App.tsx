import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { ThemeProvider, createTheme } from '@mui/material/styles'
import CssBaseline from '@mui/material/CssBaseline'
import { QueryClient, QueryClientProvider } from 'react-query'
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css'

// Layout Components
import Layout from './components/Layout/Layout'
import AuthLayout from './components/Layout/AuthLayout'

// Pages
import Home from './pages/Home'
import DeviceSelection from './pages/DeviceSelection'
import Questions from './pages/Questions'
import Pricing from './pages/Pricing'
import OrderSummary from './pages/OrderSummary'
import KycVerification from './pages/KycVerification'
import OrderConfirmation from './pages/OrderConfirmation'
import OrderTracking from './pages/OrderTracking'

// Auth Pages
import Login from './pages/auth/Login'
import Register from './pages/auth/Register'

// Dashboard Pages
import Dashboard from './pages/dashboard/Dashboard'
import Orders from './pages/dashboard/Orders'
import Profile from './pages/dashboard/Profile'

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard'
import Settings from './pages/admin/Settings'
import Models from './pages/admin/Models'
import QuestionsAdmin from './pages/admin/Questions'
import Users from './pages/admin/Users'

// Partner Pages
import PartnerDashboard from './pages/partner/PartnerDashboard'
import PartnerOrders from './pages/partner/PartnerOrders'

// Field Executive Pages
import FieldDashboard from './pages/field/FieldDashboard'
import FieldOrders from './pages/field/FieldOrders'

// Hooks and Context
import { useAuth } from './hooks/useAuth'

// Create a theme
const theme = createTheme({
  palette: {
    primary: {
      main: '#2563eb',
    },
    secondary: {
      main: '#64748b',
    },
    background: {
      default: '#f8fafc',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontSize: '2.25rem',
      fontWeight: 700,
    },
    h2: {
      fontSize: '1.875rem',
      fontWeight: 600,
    },
    h3: {
      fontSize: '1.5rem',
      fontWeight: 600,
    },
    body1: {
      fontSize: '1rem',
      lineHeight: 1.5,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          fontWeight: 600,
        },
      },
    },
  },
})

// Query Client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})

// Protected Route Component
const ProtectedRoute: React.FC<{ children: React.ReactNode; requiredRoles?: string[] }> = ({ 
  children, 
  requiredRoles 
}) => {
  const { user, isAuthenticated } = useAuth()
  
  if (!isAuthenticated) {
    return <Navigate to="/auth/login" replace />
  }
  
  if (requiredRoles && user && !requiredRoles.includes(user.role)) {
    return <Navigate to="/" replace />
  }
  
  return <>{children}</>
}

// Main App Component
function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Router>
          <div className="App">
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<Home />} />
              <Route path="/device-selection" element={<DeviceSelection />} />
              <Route path="/questions" element={<Questions />} />
              <Route path="/pricing" element={<Pricing />} />
              <Route path="/order-summary" element={<OrderSummary />} />
              <Route path="/kyc-verification" element={<KycVerification />} />
              <Route path="/order-confirmation" element={<OrderConfirmation />} />
              <Route path="/order-tracking/:orderId" element={<OrderTracking />} />

              {/* Auth Routes */}
              <Route path="/auth" element={<AuthLayout />}>
                <Route path="login" element={<Login />} />
                <Route path="register" element={<Register />} />
              </Route>

              {/* User Dashboard Routes */}
              <Route path="/dashboard" element={
                <ProtectedRoute>
                  <Layout />
                </ProtectedRoute>
              }>
                <Route index element={<Dashboard />} />
                <Route path="orders" element={<Orders />} />
                <Route path="profile" element={<Profile />} />
              </Route>

              {/* Admin Routes */}
              <Route path="/admin" element={
                <ProtectedRoute requiredRoles={['ADMIN']}>
                  <Layout />
                </ProtectedRoute>
              }>
                <Route index element={<AdminDashboard />} />
                <Route path="settings" element={<Settings />} />
                <Route path="models" element={<Models />} />
                <Route path="questions" element={<QuestionsAdmin />} />
                <Route path="users" element={<Users />} />
              </Route>

              {/* Partner Routes */}
              <Route path="/partner" element={
                <ProtectedRoute requiredRoles={['PARTNER', 'ADMIN']}>
                  <Layout />
                </ProtectedRoute>
              }>
                <Route index element={<PartnerDashboard />} />
                <Route path="orders" element={<PartnerOrders />} />
              </Route>

              {/* Field Executive Routes */}
              <Route path="/field" element={
                <ProtectedRoute requiredRoles={['FIELD_EXECUTIVE', 'ADMIN']}>
                  <Layout />
                </ProtectedRoute>
              }>
                <Route index element={<FieldDashboard />} />
                <Route path="orders" element={<FieldOrders />} />
              </Route>
            </Routes>
          </div>
        </Router>
        <ToastContainer
          position="top-right"
          autoClose={5000}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
          theme="light"
        />
      </ThemeProvider>
    </QueryClientProvider>
  )
}

export default App