import request from '@/util/request'

export const getFailureDealPage = (params) => {
  return request.get('/failureDeal/page', { params })
}

export const getFailureApprovePage = (params) => {
  return request.get('/failureDeal/approvePage', { params })
}

export const getFailureDealById = (failureId) => {
  return request.get(`/failureDeal/${failureId}`)
}

export const processFailureDeal = (data) => {
  return request.put('/failureDeal/process', data)
}

export const rollbackFailureDeal = (data) => {
  return request.put('/failureDeal/rollback', data)
}

export const approveFailureDeal = (failureId) => {
  return request.put(`/failureDeal/approve/${failureId}`)
}

export const rollbackFailureApprove = (failureId) => {
  return request.put(`/failureDeal/approveRollback/${failureId}`)
}
