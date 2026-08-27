import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { api } from '../api/client'
import type { UserProfile } from '../types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserProfile | null>(null)
  const ready = ref(false)
  const authenticated = computed(() => Boolean(user.value))
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  async function bootstrap(force = false) {
    if (ready.value && !force) return
    try {
      const { data } = await api.get<{ authenticated: boolean; user: UserProfile | Record<string, never> }>('/auth/me')
      user.value = data.authenticated ? data.user as UserProfile : null
    } catch { user.value = null } finally { ready.value = true }
  }
  async function login(payload: { login: string; password: string }) {
    const { data } = await api.post<UserProfile>('/auth/login', payload); user.value = data; ready.value = true
  }
  async function register(payload: { username: string; email: string; password: string; displayName: string }) {
    await api.post('/auth/register', payload); await login({ login: payload.username, password: payload.password })
  }
  async function logout() { await api.post('/auth/logout'); user.value = null }
  return { user, ready, authenticated, isAdmin, bootstrap, login, register, logout }
})
