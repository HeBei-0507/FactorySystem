import request from '@/util/request'

// 分页查询润滑标准
export const getLubricationStandardPage = (params) => {
  return request.get('/lubricationStandard/page', { params })
}

// 根据部位编码查询标准列表
export const getLubricationStandardListByPartCode = (partCode) => {
  return request.get(`/lubricationStandard/standardList/${partCode}`)
}

// 根据ID查询润滑标准详情
export const getLubricationStandardById = (id) => {
  return request.get(`/lubricationStandard/${id}`)
}

// 新增润滑标准
export const addLubricationStandard = (data) => {
  return request.post('/lubricationStandard/add', data)
}

// 修改润滑标准
export const updateLubricationStandard = (data) => {
  return request.put('/lubricationStandard/update', data)
}

// 删除润滑标准
export const deleteLubricationStandard = (id) => {
  return request.delete(`/lubricationStandard/${id}`)
}
