import request from '@/util/request'

export const getEquipmentPartPage = (params) => {
  return request.get('/equipmentPart/page', { params })
}

export const getEquipmentPartListByEquipmentId = (equipmentId) => {
  return request.get(`/equipmentPart/partList/${equipmentId}`)
}

export const getEquipmentPartListByDeviceCode = (equipmentId) => {
  return getEquipmentPartListByEquipmentId(equipmentId)
}

export const getEquipmentPartById = (id) => {
  return request.get(`/equipmentPart/${id}`)
}

export const addEquipmentPart = (data) => {
  return request.post('/equipmentPart/add', data)
}

export const batchAddEquipmentPart = (data) => {
  return request.post('/equipmentPart/batchAdd', data)
}

export const updateEquipmentPart = (data) => {
  return request.put('/equipmentPart/update', data)
}

export const deleteEquipmentPart = (id) => {
  return request.delete(`/equipmentPart/${id}`)
}
