/**
 * 与浏览器端一致：请求路径为 /user/login（不经 /api 前缀）。
 * 前端 Vite 将 /api 代理到后端并去掉前缀，小程序需直连后端根地址。
 *
 * 真机 / iPad 调试时请改为电脑的局域网 IP，例如 http://192.168.1.8:8080
 */
export const API_BASE_URL = 'http://localhost:8080'

/** 与 eao-frontend .env 保持一致，便于同一套账号体系 */
export const USER_TOKEN_KEY = 'app_token'
export const USER_INFO_KEY = 'app_user'
