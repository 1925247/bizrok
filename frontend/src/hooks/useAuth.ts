import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { authAPI } from '../services/api'

// Types
export interface User {
  id: number
  email: string
  name: string
  role: string
  kycVerified: boolean
}

export interface AuthContextType {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (email: string, otp: string) => Promise<void>
  logout: () => void
  sendOtp: (email: string) => Promise<void>
  clearError: () => void
  error: string | null
}

// Create context
const AuthContext = createContext<AuthContextType | undefined>(undefined)

// Auth Provider Component
export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Check authentication on mount
  useEffect(() => {
    const checkAuth = async () => {
      try {
        const token = localStorage.getItem('token')
        if (token) {
          const response = await authAPI.me()
          setUser(response.data.user)
        }
      } catch (err) {
        localStorage.removeItem('token')
      } finally {
        setIsLoading(false)
      }
    }

    checkAuth()
  }, [])

  const login = async (email: string, otp: string) => {
    setIsLoading(true)
    setError(null)
    
    try {
      const response = await authAPI.login({ email, otp })
      const { token, user: userData } = response.data
      
      localStorage.setItem('token', token)
      setUser(userData)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed')
      throw err
    } finally {
      setIsLoading(false)
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    setUser(null)
  }

  const sendOtp = async (email: string) => {
    try {
      await authAPI.sendOtp(email)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to send OTP')
      throw err
    }
  }

  const clearError = () => {
    setError(null)
  }

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    logout,
    sendOtp,
    clearError,
    error,
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

// Auth Hook
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

// Protected Route Component
export const ProtectedRoute: React.FC<{ children: ReactNode; requiredRoles?: string[] }> = ({ 
  children, 
  requiredRoles 
}) => {
  const { user, isAuthenticated } = useAuth()
  
  if (!isAuthenticated) {
    // Redirect to login would be handled by router
    return null
  }
  
  if (requiredRoles && user && !requiredRoles.includes(user.role)) {
    // Redirect to unauthorized would be handled by router
    return null
  }
  
  return <>{children}</>
}