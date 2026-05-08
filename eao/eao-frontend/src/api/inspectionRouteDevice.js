import request from '@/util/request'

export const getInspectionRouteDeviceList = (routeCode) => {
  return request.get(`/inspectionRouteDevice/list/${encodeURIComponent(routeCode)}`)
}

export const getInspectionRouteDevicePage = (params) => {
  return request.get('/inspectionRouteDevice/page', { params })
}

export const addInspectionRouteDevice = (data) => {
  return request.post('/inspectionRouteDevice/addInspectionRouteDevice', data)
}

export const updateInspectionRouteDevice = (data) => {
  return request.put('/inspectionRouteDevice/updateInspectionRouteDevice', data)
}

export const batchDeleteInspectionRouteDevice = (ids) => {
  return request.delete('/inspectionRouteDevice/batchDeleteInspectionRouteDevice', { data: ids })
}
