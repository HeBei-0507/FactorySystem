import request from '@/util/request'

export const getMaintenancePlanPage = (params) => {
  return request.get('/maintenancePlan/page', { params })
}

export const getMaintenancePlanById = (id) => {
  return request.get(`/maintenancePlan/${id}`)
}

export const addMaintenancePlan = (data) => {
  return request.post('/maintenancePlan/add', data)
}

export const updateMaintenancePlan = (data) => {
  return request.put('/maintenancePlan/update', data)
}

export const batchDeleteMaintenancePlan = (ids) => {
  return request.delete('/maintenancePlan/batchDelete', { data: ids })
}

export const submitMaintenancePlan = (ids) => {
  return request.post('/maintenancePlan/submit', ids)
}

export const approveMaintenancePlan = (ids) => {
  return request.post('/maintenancePlan/approve', ids)
}

export const rollbackMaintenancePlan = (ids) => {
  return request.post('/maintenancePlan/rollback', ids)
}
