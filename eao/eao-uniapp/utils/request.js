import { API_BASE_URL } from './config.js'
import { getToken, clearAuth } from './auth.js'
import { unwrapData } from './result.js'

function joinUrl(base, path) {
  const b = base.replace(/\/$/, '')
  const p = path.startsWith('/') ? path : `/${path}`
  return `${b}${p}`
}

/**
 * @param {{ url: string, method?: string, data?: object, skipAuth?: boolean }} options
 */
export function request(options) {
  const { url, method = 'GET', data, skipAuth } = options
  const header = {
    'Content-Type': 'application/json'
  }
  if (!skipAuth) {
    const token = getToken()
    if (token) header.Authorization = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: joinUrl(API_BASE_URL, url),
      method,
      data: data ?? {},
      header,
      timeout: 15000,
      success: (res) => {
        const { statusCode, data: body } = res
        if (statusCode === 401 || statusCode === 403) {
          clearAuth()
          reject(
            Object.assign(new Error(statusCode === 401 ? '未授权' : '无权限'), {
              statusCode
            })
          )
          return
        }
        if (statusCode < 200 || statusCode >= 300) {
          const msg =
            body && typeof body === 'object' && body.message != null
              ? String(body.message)
              : `HTTP ${statusCode}`
          reject(Object.assign(new Error(msg), { statusCode, body }))
          return
        }
        try {
          resolve(unwrapData(body))
        } catch (e) {
          reject(e)
        }
      },
      fail: (err) => {
        reject(
          Object.assign(new Error(err?.errMsg || '网络请求失败'), {
            original: err
          })
        )
      }
    })
  })
}
