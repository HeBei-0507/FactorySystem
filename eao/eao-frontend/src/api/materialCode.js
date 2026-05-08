import request from '@/util/request'

// 分页查询物料代码
export const getMaterialCodePage = (params) => {
  return request.get('/materialCode/page', { params })
}

// 新增物料代码
export const addMaterialCode = (data) => {
  return request.post('/materialCode/addMaterialCode', data)
}

// 修改物料代码
export const updateMaterialCode = (data) => {
  return request.put('/materialCode/updateMaterialCode', data)
}

// 删除物料代码
export const deleteMaterialCode = (id) => {
  return request.delete(`/materialCode/deleteMaterialCode/${id}`)
}

// 批量删除物料代码
export const batchDeleteMaterialCode = (ids) => {
  return request.delete('/materialCode/batchDeleteMaterialCode', { data: ids })
}

// 根据ID查询物料代码详情
export const getMaterialCodeById = (id) => {
  return request.get(`/materialCode/${id}`)
}
