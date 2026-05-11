/**
 * 解析 Spring Result：{ code, message, data }
 */
export function unwrapData(body) {
  if (body == null) return null
  if (typeof body !== 'object' || !('code' in body)) {
    return body
  }
  const c = Number(body.code)
  if (c === 200 || c === 0) {
    return 'data' in body ? body.data : body
  }
  const msg =
    typeof body.message === 'string' && body.message ? body.message : '请求失败'
  const err = new Error(msg)
  err.body = body
  err.code = c
  err.isBusinessError = true
  throw err
}
