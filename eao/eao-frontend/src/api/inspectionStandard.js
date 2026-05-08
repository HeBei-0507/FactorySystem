import request from '@/util/request'

// 分页查询点检标准
export const getInspectionStandardPage = (params) => {
  return request.get('/inspectionStandard/page', { params })
}

// 根据部位编码查询点检标准简表
export const getInspectionStandardListByPartCode = (partCode) => {
  return request.get(`/inspectionStandard/standardList/${partCode}`)
}

// 根据 ID 查询点检标准详情
export const getInspectionStandardById = (id) => {
  return request.get(`/inspectionStandard/${id}`)
}

// 新增点检标准
export const addInspectionStandard = (data) => {
  return request.post('/inspectionStandard/add', data)
}

// 修改点检标准
export const updateInspectionStandard = (data) => {
  return request.put('/inspectionStandard/update', data)
}

// 删除点检标准
export const deleteInspectionStandard = (id) => {
  return request.delete(`/inspectionStandard/${id}`)
}

// 批量新增点检标准
export const batchAddInspectionStandard = (data) => {
  return request.post('/inspectionStandard/batchAdd', data)
}
