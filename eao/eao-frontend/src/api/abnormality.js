import request from '@/util/request'

export const getAbnormalityPage = (params) => {
  return request.get('/abnormality/page', { params })
}

export const addAbnormality = (data) => {
  return request.post('/abnormality/add', data)
}

export const updateAbnormality = (data) => {
  return request.put('/abnormality/update', data)
}

export const deleteAbnormality = (id) => {
  return request.delete(`/abnormality/${id}`)
}

export const batchDeleteAbnormality = (ids) => {
  return request.delete('/abnormality/batchDelete', { data: ids })
}

export const submitAbnormality = (ids) => {
  return request.put('/abnormality/submit', ids)
}

export const getAbnormalityById = (id) => {
  return request.get(`/abnormality/${id}`)
}
