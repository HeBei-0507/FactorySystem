import { request } from '../utils/request.js'

/** POST /user/login */
export function login(data) {
  return request({
    url: '/user/login',
    method: 'POST',
    data,
    skipAuth: true
  })
}

/** POST /user/logout（需登录；失败时客户端仍会清本地态） */
export function logout() {
  return request({
    url: '/user/logout',
    method: 'POST',
    data: {}
  })
}
