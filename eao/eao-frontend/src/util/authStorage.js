function tokenKey() {
  return import.meta.env.VITE_USER_TOKEN_KEY
}

function userInfoKey() {
  return import.meta.env.VITE_USER_INFO_KEY
}

export function getUserProfile() {
  const k = userInfoKey()
  if (!k) return null
  try {
    const raw = localStorage.getItem(k)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function setUserProfile(profile) {
  const k = userInfoKey()
  if (k && profile && typeof profile === 'object') {
    localStorage.setItem(k, JSON.stringify(profile))
  }
}

export function clearAuth() {
  const tk = tokenKey()
  if (tk) localStorage.removeItem(tk)
  const uk = userInfoKey()
  if (uk) localStorage.removeItem(uk)
}
