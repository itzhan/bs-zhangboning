import axios from 'axios'
import { useUserStore } from '@/stores/user'

const instance = axios.create({
  baseURL: '',
  timeout: 15000
})

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200 || res.code === 0 || response.status === 200) {
      return res
    }
    window.$message?.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.$message?.error('登录已过期，请重新登录')
      window.location.hash = '#/login'
    } else {
      window.$message?.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export function get<T = any>(url: string, params?: Record<string, any>): Promise<T> {
  return instance.get(url, { params }) as any
}

export function post<T = any>(url: string, data?: Record<string, any>): Promise<T> {
  return instance.post(url, data) as any
}

export function put<T = any>(url: string, data?: Record<string, any>): Promise<T> {
  return instance.put(url, data) as any
}

export function del<T = any>(url: string): Promise<T> {
  return instance.delete(url) as any
}

export default instance
