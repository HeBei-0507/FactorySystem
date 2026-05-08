import request from '@/util/request'

export const getWarehouseLocationPage = (params) =>
  request.get('/warehouseLocation/page', { params })
export const getWarehouseLocationListByProductionLine = (productionLineId) =>
  request.get('/warehouseLocation/listByProductionLine', { params: { productionLineId } })
export const addWarehouseLocation = (data) => request.post('/warehouseLocation/add', data)
export const updateWarehouseLocation = (data) => request.put('/warehouseLocation/update', data)
export const batchDeleteWarehouseLocation = (ids) =>
  request.delete('/warehouseLocation/batchDelete', { data: ids })
