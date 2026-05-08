/**
 * 解析 Spring Result 风格：{ code, message, data }；HTTP 200 时仍可能为业务错误。
 * 无 code 字段时视为已解包结构，原样返回。
 */
export function unwrapData(body) {
  if (body == null) return null
  if (typeof body !== 'object' || !('code' in body)) {
    return body
  }
  const c = body.code
  if (c === 200 || c === 0) {
    return 'data' in body ? body.data : body
  }
  const err = new Error(
    typeof body.message === 'string' && body.message ? body.message : '请求失败'
  )
  err.body = body
  err.code = c
  err.isBusinessError = true
  throw err
}
