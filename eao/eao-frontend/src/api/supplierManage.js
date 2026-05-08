import request from '@/util/request'

export const getSupplierManagePage = (params) => {
  return request.get('/supplierManage/page', { params })
}

export const addSupplierManage = (data) => {
  return request.post('/supplierManage/addSupplierManage', data)
}

export const updateSupplierManage = (data) => {
  return request.put('/supplierManage/updateSupplierManage', data)
}

export const deleteSupplierManage = (id) => {
  return request.delete(`/supplierManage/deleteSupplierManage/${id}`)
}

export const batchDeleteSupplierManage = (ids) => {
  return request.delete('/supplierManage/batchDeleteSupplierManage', { data: ids })
}

export const getSupplierManageById = (id) => {
  return request.get(`/supplierManage/${id}`)
}
