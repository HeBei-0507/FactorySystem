import request from '@/util/request'

export const getMaintenanceStandardPage = (params) => {
  return request.get('/maintenanceStandard/page', { params })
}

export const getMaintenanceStandardById = (id) => {
  return request.get(`/maintenanceStandard/${id}`)
}

export const addMaintenanceStandard = (data) => {
  return request.post('/maintenanceStandard/add', data)
}

export const updateMaintenanceStandard = (data) => {
  return request.put('/maintenanceStandard/update', data)
}

export const deleteMaintenanceStandard = (id) => {
  return request.delete(`/maintenanceStandard/${id}`)
}

export const getMaintenanceMaterialCandidatePage = (params) => {
  return request.get('/maintenanceStandard/materialCandidate/page', { params })
}
