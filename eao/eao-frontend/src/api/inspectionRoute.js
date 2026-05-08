import request from '@/util/request'

export const getInspectionRouteList = (productionLineId) => {
  return request.get(`/inspectionRoute/list/${productionLineId}`)
}

export const getInspectionRoutePage = (params) => {
  return request.get('/inspectionRoute/page', { params })
}

export const addInspectionRoute = (data) => {
  return request.post('/inspectionRoute/addInspectionRoute', data)
}

export const updateInspectionRoute = (data) => {
  return request.put('/inspectionRoute/updateInspectionRoute', data)
}

export const batchDeleteInspectionRoute = (ids) => {
  return request.delete('/inspectionRoute/batchDeleteInspectionRoute', { data: ids })
}
