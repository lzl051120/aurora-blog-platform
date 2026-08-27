import axios, { AxiosError } from 'axios'

export const api = axios.create({
  baseURL: '/api/v1', withCredentials: true, timeout: 15_000,
  xsrfCookieName: 'XSRF-TOKEN', xsrfHeaderName: 'X-XSRF-TOKEN', withXSRFToken: true,
})

let csrfReady = false
export async function ensureCsrf() {
  if (!csrfReady || !document.cookie.includes('XSRF-TOKEN=')) {
    await api.get('/auth/csrf'); csrfReady = true
  }
}
api.interceptors.request.use(async (config) => {
  const method = config.method?.toLowerCase()
  if (method && ['post', 'put', 'patch', 'delete'].includes(method) && !config.url?.endsWith('/auth/csrf')) await ensureCsrf()
  return config
})
export function apiMessage(error: unknown, fallback = '操作失败，请稍后重试') {
  if (error instanceof AxiosError) return (error.response?.data as { message?: string } | undefined)?.message || fallback
  return fallback
}
