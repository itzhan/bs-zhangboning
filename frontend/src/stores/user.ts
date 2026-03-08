import { defineStore } from 'pinia'
import { ref } from 'vue'
import { post, get } from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<Record<string, any>>(
    JSON.parse(localStorage.getItem('userInfo') || '{}')
  )

  async function login(username: string, password: string) {
    const res: any = await post('/api/auth/login', { username, password })
    token.value = res.data?.token || res.token || ''
    localStorage.setItem('token', token.value)
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    try {
      const res: any = await get('/api/users/me')
      userInfo.value = res.data || res
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } catch {
      // ignore
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, login, logout, fetchUserInfo }
})
