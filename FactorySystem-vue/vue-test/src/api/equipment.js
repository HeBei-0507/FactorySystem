import request from "@/util/request"

// 分页查询设备
export const getEquipmentPage = (params) => {
  return request.get('/equipment/page', { params })
}

// 根据 ID 查询设备详情
export const getEquipmentById = (id) => {
  return request.get(`/equipment/${id}`)
}

// 新增设备
export const addEquipment = (data) => {
  return request.post('/equipment/add', data)
}

// 修改设备
export const updateEquipment = (data) => {
  return request.put('/equipment/update', data)
}

// 删除设备
export const deleteEquipment = (id) => {
  return request.delete(`/equipment/${id}`)
}
