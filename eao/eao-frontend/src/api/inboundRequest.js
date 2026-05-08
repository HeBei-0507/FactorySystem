import request from '@/util/request'

export const getInboundRequestPage = (params) => request.get('/inboundRequest/page', { params })
export const addInboundRequest = (data) => request.post('/inboundRequest/add', data)
export const updateInboundRequest = (data) => request.put('/inboundRequest/update', data)
export const confirmInboundRequest = (ids) => request.put('/inboundRequest/confirmInbound', ids)
export const batchDeleteInboundRequest = (ids) =>
  request.delete('/inboundRequest/batchDelete', { data: ids })
