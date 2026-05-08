/**
 * 与后端统一 Result 配合： data 为 { total, records } 时取列表；
 * 若 data 为直出数组，原样使用。
 */
export function extractRecordsArray(dataPayload) {
  if (dataPayload == null) return []
  if (Array.isArray(dataPayload)) return dataPayload
  if (typeof dataPayload === 'object' && Array.isArray(dataPayload.records)) {
    return dataPayload.records
  }
  return []
}

/**
 * 从完整响应（body 上含 code、data 等）中解析 data 里的分页/列表
 */
export function extractPageData(res) {
  const d = res?.data
  if (d == null) return { records: [], total: 0 }
  if (Array.isArray(d)) {
    return { records: d, total: d.length }
  }
  const records = Array.isArray(d.records) ? d.records : []
  const total = typeof d.total === 'number' ? d.total : records.length
  return { records, total }
}
