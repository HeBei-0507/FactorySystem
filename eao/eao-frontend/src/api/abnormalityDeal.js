import request from '@/util/request'

export const getAbnormalityDealPage = (params) => {
  return request.get('/abnormalityDeal/page', { params })
}

export const getAbnormalityDealById = (abnormalityId) => {
  return request.get(`/abnormalityDeal/${abnormalityId}`)
}

export const processAbnormalityDeal = (data) => {
  return request.put('/abnormalityDeal/process', data)
}

export const rollbackAbnormalityDeal = (data) => {
  return request.put('/abnormalityDeal/rollback', data)
}

export const getAbnormalityApprovePage = (params) => {
  return request.get('/abnormalityDeal/approvePage', { params })
}

export const approveAbnormalityDeal = (abnormalityId) => {
  return request.put(`/abnormalityDeal/approve/${abnormalityId}`)
}

export const rollbackAbnormalityApprove = (abnormalityId) => {
  return request.put(`/abnormalityDeal/approveRollback/${abnormalityId}`)
}
