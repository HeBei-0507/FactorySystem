import request from '@/util/request'

export const getEquipmentPage = (params) => {
  return request.get('/equipment/page', { params })
}

export const getEquipmentPageByLine = (lineCode, params) => {
  return request.get(`/equipment/line/${lineCode}`, { params })
}

export const getEquipmentById = (id) => {
  return request.get(`/equipment/${id}`)
}

export const getEquipmentListByDeviceUnitId = (deviceUnitId) => {
  return request.get(`/equipment/equipmentList/${deviceUnitId}`)
}

export const getEquipmentListByDeviceUnitCode = (deviceUnitId) => {
  return getEquipmentListByDeviceUnitId(deviceUnitId)
}

export const addEquipment = (data) => {
  return request.post('/equipment/add', data)
}

export const batchAddEquipment = (data) => {
  return request.post('/equipment/batchAdd', data)
}

export const updateEquipment = (data) => {
  return request.put('/equipment/update', data)
}

export const deleteEquipment = (id) => {
  return request.delete(`/equipment/${id}`)
}

export const batchDeleteEquipment = (ids) => {
  return request.delete('/equipment/batchDel', { data: ids })
}
