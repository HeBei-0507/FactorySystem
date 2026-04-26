import request from "@/util/request"

// 设备管理单元相关接口

// 获取生产线的设备管理单元列表
export const getDeviceUtilList = (productionLineId) => {
    return request.get(`/deviceUnit/getDeviceUtilList/${productionLineId}`)
}

// 分页查询设备管理单元
export const getDeviceUnitPage = (params) => {
    return request.get('/deviceUnit/page', { params })
}

// 添加设备管理单元
export const addDeviceUnit = (data) => {
    return request.post('/deviceUnit/addDeviceUnit', data)
}

// 修改设备管理单元
export const updateDeviceUnit = (data) => {
    return request.put('/deviceUnit/updateDeviceUnit', data)
}

// 批量删除设备管理单元
export const batchDeleteDeviceUnit = (ids) => {
    return request.delete('/deviceUnit/deleteDeviceUnit', { data: ids })
}
