import axios from 'axios'

// Base API configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/auth/login'
    }
    return Promise.reject(error)
  }
)

// Auth API endpoints
export const authAPI = {
  login: (data: { email: string; otp: string }) =>
    api.post('/auth/login', data),
  
  sendOtp: (email: string) =>
    api.post('/auth/send-otp', { email }),
  
  me: () =>
    api.get('/user/profile'),
}

// Device API endpoints
export const deviceAPI = {
  getCategories: () =>
    api.get('/public/categories'),
  
  getBrands: () =>
    api.get('/public/brands'),
  
  getModels: (categoryId?: number, brandId?: number) =>
    api.get('/public/models', {
      params: { categoryId, brandId }
    }),
  
  getModel: (id: number) =>
    api.get(`/public/models/${id}`),
}

// Question API endpoints
export const questionAPI = {
  getQuestions: () =>
    api.get('/public/questions'),
  
  getQuestionsByGroup: (groupId: number) =>
    api.get(`/public/questions/group/${groupId}`),
  
  getQuestionsBySubGroup: (subGroupId: number) =>
    api.get(`/public/questions/subgroup/${subGroupId}`),
}

// Order API endpoints
export const orderAPI = {
  createOrder: (data: any) =>
    api.post('/user/orders', data),
  
  getOrder: (id: number) =>
    api.get(`/user/orders/${id}`),
  
  getOrders: () =>
    api.get('/user/orders'),
  
  updateOrderStatus: (id: number, status: string) =>
    api.put(`/user/orders/${id}/status`, { status }),
  
  submitKyc: (data: FormData) =>
    api.post('/user/kyc/submit', data, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
  
  getKycStatus: () =>
    api.get('/user/kyc/status'),
}

// Pricing API endpoints
export const pricingAPI = {
  calculatePrice: (modelId: number, answers: any[]) =>
    api.post('/public/pricing/calculate', {
      modelId,
      answers
    }),
  
  getPriceHistory: (orderId: number) =>
    api.get(`/user/orders/${orderId}/price-history`),
}

// Admin API endpoints
export const adminAPI = {
  // Settings
  getSettings: () =>
    api.get('/admin/settings'),
  
  updateSetting: (key: string, value: string, active: boolean) =>
    api.put('/admin/settings', { key, value, active }),
  
  toggleFeature: (key: string, enable: boolean) =>
    api.put(`/admin/settings/toggle/${key}`, { enable }),
  
  // Models
  getModels: () =>
    api.get('/admin/models'),
  
  createModel: (data: any) =>
    api.post('/admin/models', data),
  
  updateModel: (id: number, data: any) =>
    api.put(`/admin/models/${id}`, data),
  
  deleteModel: (id: number) =>
    api.delete(`/admin/models/${id}`),
  
  // Questions
  getQuestions: () =>
    api.get('/admin/questions'),
  
  createQuestion: (data: any) =>
    api.post('/admin/questions', data),
  
  updateQuestion: (id: number, data: any) =>
    api.put(`/admin/questions/${id}`, data),
  
  deleteQuestion: (id: number) =>
    api.delete(`/admin/questions/${id}`),
  
  // Options
  createOption: (data: any) =>
    api.post('/admin/options', data),
  
  updateOption: (id: number, data: any) =>
    api.put(`/admin/options/${id}`, data),
  
  deleteOption: (id: number) =>
    api.delete(`/admin/options/${id}`),
  
  // Orders
  getOrders: () =>
    api.get('/admin/orders'),
  
  getOrdersByStatus: (status: string) =>
    api.get(`/admin/orders/status/${status}`),
  
  updateOrderStatus: (id: number, status: string, notes?: string) =>
    api.put(`/admin/orders/${id}/status`, { status, notes }),
  
  assignOrder: (id: number, userId: number) =>
    api.put(`/admin/orders/${id}/assign`, { userId }),
  
  // Users
  getUsers: () =>
    api.get('/admin/users'),
  
  getUsersByRole: (role: string) =>
    api.get(`/admin/users/role/${role}`),
  
  updateUserRole: (id: number, role: string) =>
    api.put(`/admin/users/${id}/role`, { role }),
  
  toggleUserActive: (id: number, active: boolean) =>
    api.put(`/admin/users/${id}/active`, { active }),
  
  // KYC
  getPendingKycDocuments: () =>
    api.get('/admin/kyc/pending'),
  
  verifyKycDocument: (id: number, verified: boolean, notes: string) =>
    api.put(`/admin/kyc/${id}/verify`, { verified, notes }),
  
  // Dashboard
  getDashboardStats: () =>
    api.get('/admin/dashboard/stats'),
}

// Partner API endpoints
export const partnerAPI = {
  getOrders: () =>
    api.get('/partner/orders'),
  
  getAssignedOrders: () =>
    api.get('/partner/orders/assigned'),
  
  acceptOrder: (id: number) =>
    api.put(`/partner/orders/${id}/accept`),
  
  rejectOrder: (id: number, reason: string) =>
    api.put(`/partner/orders/${id}/reject`, { reason }),
  
  updateOrderDetails: (id: number, data: any) =>
    api.put(`/partner/orders/${id}/details`, data),
  
  completeOrder: (id: number) =>
    api.put(`/partner/orders/${id}/complete`),
}

// Field Executive API endpoints
export const fieldAPI = {
  getOrders: () =>
    api.get('/field/orders'),
  
  getAssignedOrders: () =>
    api.get('/field/orders/assigned'),
  
  updateOrderStatus: (id: number, status: string) =>
    api.put(`/field/orders/${id}/status`, { status }),
  
  updateOrderDetails: (id: number, data: any) =>
    api.put(`/field/orders/${id}/details`, data),
  
  completeOrder: (id: number, data: FormData) =>
    api.put(`/field/orders/${id}/complete`, data, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
}

export default api