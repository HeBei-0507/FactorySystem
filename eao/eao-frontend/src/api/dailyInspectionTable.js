import request from '@/util/request'

// 分页查询日常巡检表
export const getDailyInspectionTablePage = (params) => {
  return request.get('/dailyInspectionTable/page', { params })
}

// 新增日常巡检表
export const addDailyInspectionTable = (data) => {
  return request.post('/dailyInspectionTable/addDailyInspectionTable', data)
}

// 修改日常巡检表
export const updateDailyInspectionTable = (data) => {
  return request.put('/dailyInspectionTable/updateDailyInspectionTable', data)
}

// 批量删除日常巡检表
export const batchDeleteDailyInspectionTable = (ids) => {
  return request.delete('/dailyInspectionTable/batchDeleteDailyInspectionTable', { data: ids })
}
