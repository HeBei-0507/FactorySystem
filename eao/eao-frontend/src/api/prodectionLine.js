import request from '@/util/request'

// 设备管理单元相关接口

// 获取生产线列表
export const getProductionlineList = () => {
  return request.get('/productionLine/getProductionLineList')
}
