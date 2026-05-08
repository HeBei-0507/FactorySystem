import request from '@/util/request'

export const getMaintenanceTicketPage = (params) => {
  return request.get('/maintenanceTicket/page', { params })
}

export const getMaintenanceTicketById = (id) => {
  return request.get(`/maintenanceTicket/${id}`)
}

export const addMaintenanceTicket = (data) => {
  return request.post('/maintenanceTicket/add', data)
}

export const updateMaintenanceTicket = (data) => {
  return request.put('/maintenanceTicket/update', data)
}

export const batchDeleteMaintenanceTicket = (ids) => {
  return request.delete('/maintenanceTicket/batchDelete', { data: ids })
}

export const submitMaintenanceTicket = (ids) => {
  return request.post('/maintenanceTicket/submit', ids)
}

export const approveMaintenanceTicket = (ids) => {
  return request.post('/maintenanceTicket/approve', ids)
}

export const rollbackMaintenanceTicket = (ids) => {
  return request.post('/maintenanceTicket/rollback', ids)
}
