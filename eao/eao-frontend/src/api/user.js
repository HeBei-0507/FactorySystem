import request from '@/util/request'

/**
 * 登录（对应 SysUserController POST /user/login）
 * 登录接口失败（401/403 等）使用 skipAuthRedirect，由页面自行提示。
 */
export const login = (data) => {
  return request.post('/user/login', data, { skipAuthRedirect: true })
}

export const register = (data) => {
  return request.post('/user/register', data, { skipAuthRedirect: true })
}

export const getUserInfo = (id) => {
  return request.get(`/user/info/${id}`)
}

export const updateUserInfo = (data) => {
  return request.put('/user/update', data)
}

export const logout = () => {
  return request.post('/user/logout')
}
