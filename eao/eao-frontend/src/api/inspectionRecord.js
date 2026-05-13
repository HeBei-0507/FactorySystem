import request from '@/util/request'

export const getInspectionRecordPage = (params) => {
  return request.get('/inspectionRecord/page', { params })
}

export const getInspectionRecordById = (id) => {
  return request.get(`/inspectionRecord/${id}`)
}

export const addInspectionRecord = (data) => {
  return request.post('/inspectionRecord/add', data)
}

export const updateInspectionRecord = (data) => {
  return request.put('/inspectionRecord/update', data)
}

export const deleteInspectionRecord = (id) => {
  return request.delete(`/inspectionRecord/${id}`)
}

export const batchDeleteInspectionRecord = (ids) => {
  return request.delete('/inspectionRecord/batchDelete', { data: ids })
}

export const completeInspectionRecord = (ids) => {
  return request.put('/inspectionRecord/complete', { ids })
}
