package com.hebei.systemdemo.service;

import com.hebei.systemdemo.DTO.Result;
import com.hebei.systemdemo.pojo.DeviceUnit;

public interface IDeviceUnitService {
    Result getDeviceUnitListByProductionLineId(Long productionLineId);

    Result addDeviceUnit(DeviceUnit deviceUnit);

    Result page(Integer current, Integer size, Long productionLineId, String unitCode, String unitName);
}
