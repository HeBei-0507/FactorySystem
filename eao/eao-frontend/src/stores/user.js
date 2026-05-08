import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getUserProfile, setUserProfile, clearAuth } from '@/util/authStorage'

function normalizeId(p) {
  if (p == null) return null
  return p.userId != null ? p.userId : p.id != null ? p.id : null
}

export const useUserStore = defineStore('user', () => {
  const profile = ref(/** @type {null | Record<string, unknown>} */ (null))

  const userId = computed(() => normalizeId(profile.value))

  const displayName = computed(() => {
    const p = profile.value
    if (!p) return '用户'
    const rn = p.realName
    const un = p.username
    if (typeof rn === 'string' && rn) return rn
    if (typeof un === 'string' && un) return un
    return '用户'
  })

  /** 从 localStorage 恢复（用于刷新页） */
  function initFromStorage() {
    profile.value = getUserProfile()
  }

  function setFromLoginResponse(login) {
    if (!login) return
    const next = {
      userId: login.userId,
      username: login.username,
      realName: login.realName
    }
    profile.value = next
    setUserProfile(next)
  }

  /** 拉取后端用户后合并进 store 并写回 localStorage */
  function mergeFromServerUser(u) {
    if (u == null || typeof u !== 'object') return
    const uid = u.userId != null ? u.userId : u.id
    if (uid == null) return
    const next = {
      userId: uid,
      username: u.username,
      realName: u.realName,
      phone: u.phone,
      status: u.status
    }
    profile.value = { ...(profile.value || {}), ...next }
    setUserProfile(profile.value)
  }

  function clearUser() {
    profile.value = null
    clearAuth()
  }

  return {
    profile,
    userId,
    displayName,
    initFromStorage,
    setFromLoginResponse,
    mergeFromServerUser,
    clearUser
  }
})
