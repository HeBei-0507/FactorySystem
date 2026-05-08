import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getActivePinia } from 'pinia'
import { useUserStore } from '@/stores/user'
import { clearAuth } from '@/util/authStorage'

const baseURL = '/api'

/** 与后端 Result.code 一致时视为成功 */
const SUCCESS_BUSINESS_CODES = [200, 0]

const instance = axios.create({
  baseURL,
  timeout: 10000
})

function markNotified(e) {
  e.elMessageNotified = true
  return e
}

/**
 * 从错误响应中解析可展示的文案（含 Spring Result / 纯字符串体）
 */
function pickHttpErrorMessage(err) {
  const d = err?.response?.data
  if (d == null) return ''
  if (typeof d === 'string' && d.trim()) return d.slice(0, 200)
  if (typeof d === 'object' && d.message != null) return String(d.message)
  return ''
}

function isBusinessSuccess(data) {
  if (data == null || typeof data !== 'object' || !('code' in data)) {
    return true
  }
  return SUCCESS_BUSINESS_CODES.includes(Number(data.code))
}

instance.interceptors.request.use(
  (config) => {
    const tokenKey = import.meta.env.VITE_USER_TOKEN_KEY
    if (tokenKey) {
      const token = localStorage.getItem(tokenKey)
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  (err) => Promise.reject(err)
)

// 响应拦截器：HTTP 2xx 但 body 中 Result.code 非 200/0 时统一提示
instance.interceptors.response.use(
  (res) => {
    const { data, status, statusText } = res
    if (!isBusinessSuccess(data)) {
      const d = data
      const msg =
        d && typeof d === 'object' && typeof d.message === 'string' && d.message
          ? d.message
          : '操作失败'
      ElMessage.error(msg)
      return Promise.reject(
        markNotified(
          Object.assign(new Error(String(msg)), {
            isBusinessError: true,
            body: d,
            response: { data, status, statusText }
          })
        )
      )
    }
    return data
  },
  (err) => {
    if (axios.isCancel(err)) {
      return Promise.reject(err)
    }
    if (err.config?.skipAuthRedirect) {
      if (!err.response) {
        const msg = err.message?.toLowerCase().includes('timeout')
          ? '请求超时，请稍后重试'
          : '网络异常，请检查连接'
        ElMessage.error(msg)
        return Promise.reject(markNotified(new Error(msg)))
      }
      const st = err.response.status
      if (st === 401 || st === 403) {
        return Promise.reject(err)
      }
      const msg = pickHttpErrorMessage(err) || `请求失败（${st}）`
      ElMessage.error(msg)
      return Promise.reject(
        markNotified(Object.assign(new Error(String(msg)), { response: err.response }))
      )
    }
    if (err.response) {
      const st = err.response.status
      if (st === 401 || st === 403) {
        const pinia = getActivePinia()
        if (pinia) {
          useUserStore(pinia).clearUser()
        } else {
          clearAuth()
        }
        const msg = st === 401 ? '登录已过期或无效，请重新登录' : '无访问权限，请重新登录'
        if (router.currentRoute.value.path !== '/login') {
          ElMessage.error(msg)
          err.elMessageNotified = true
          router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath }
          })
        } else {
          ElMessage.error(msg)
          err.elMessageNotified = true
        }
        return Promise.reject(err)
      }
      const msg = pickHttpErrorMessage(err) || `请求失败（${st}）`
      ElMessage.error(msg)
      err.elMessageNotified = true
      return Promise.reject(err)
    }
    const msg = err.message?.toLowerCase().includes('timeout')
      ? '请求超时，请稍后重试'
      : '网络异常，请检查连接'
    ElMessage.error(msg)
    return Promise.reject(markNotified(Object.assign(new Error(String(msg)), { original: err })))
  }
)

export default instance
export { baseURL, SUCCESS_BUSINESS_CODES, isBusinessSuccess }
