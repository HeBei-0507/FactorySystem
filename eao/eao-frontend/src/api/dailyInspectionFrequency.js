import request from '@/util/request'

// 分页查询日常巡检频次
export const getDailyInspectionFrequencyPage = (params) => {
  return request.get('/dailyInspectionFrequency/page', { params })
}

// 新增日常巡检频次
export const addDailyInspectionFrequency = (data) => {
  return request.post('/dailyInspectionFrequency/addDailyInspectionFrequency', data)
}

// 修改日常巡检频次
export const updateDailyInspectionFrequency = (data) => {
  return request.put('/dailyInspectionFrequency/updateDailyInspectionFrequency', data)
}

// 批量删除日常巡检频次
export const batchDeleteDailyInspectionFrequency = (ids) => {
  return request.delete('/dailyInspectionFrequency/batchDeleteDailyInspectionFrequency', {
    data: ids
  })
}
