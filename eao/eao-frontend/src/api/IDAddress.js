import request from '@/util/request'

export const getIDAddressPage = (params) => {
  return request.get('/idAddress/page', { params })
}

export const addIDAddress = (data) => {
  return request.post('/idAddress/addIDAddress', data)
}

export const updateIDAddress = (data) => {
  return request.put('/idAddress/updateIDAddress', data)
}

export const deleteIDAddress = (id) => {
  return request.delete(`/idAddress/deleteIDAddress/${id}`)
}

export const batchDeleteIDAddress = (ids) => {
  return request.delete('/idAddress/batchDeleteIDAddress', { data: ids })
}

export const getIDAddressById = (id) => {
  return request.get(`/idAddress/${id}`)
}
