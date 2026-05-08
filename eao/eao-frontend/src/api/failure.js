import request from '@/util/request'

export const getFailurePage = (params) => {
  return request.get('/failure/page', { params })
}

export const addFailure = (data) => {
  return request.post('/failure/add', data)
}

export const updateFailure = (data) => {
  return request.put('/failure/update', data)
}

export const deleteFailure = (id) => {
  return request.delete(`/failure/${id}`)
}

export const batchDeleteFailure = (ids) => {
  return request.delete('/failure/batchDelete', { data: ids })
}

export const submitFailure = (ids) => {
  return request.put('/failure/submit', ids)
}

export const getFailureById = (id) => {
  return request.get(`/failure/${id}`)
}
