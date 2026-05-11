import { USER_TOKEN_KEY, USER_INFO_KEY } from './config.js'

export function getToken() {
  try {
    return uni.getStorageSync(USER_TOKEN_KEY) || ''
  } catch {
    return ''
  }
}

export function getUserProfile() {
  try {
    const raw = uni.getStorageSync(USER_INFO_KEY)
    if (!raw) return null
    return typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch {
    return null
  }
}

export function setAuth(token, profile) {
  if (token) uni.setStorageSync(USER_TOKEN_KEY, token)
  if (profile && typeof profile === 'object') {
    uni.setStorageSync(USER_INFO_KEY, JSON.stringify(profile))
  }
}

export function clearAuth() {
  try {
    uni.removeStorageSync(USER_TOKEN_KEY)
    uni.removeStorageSync(USER_INFO_KEY)
  } catch {
    // ignore
  }
}

export function isLoggedIn() {
  return Boolean(getToken())
}
