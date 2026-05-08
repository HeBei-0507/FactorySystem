package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.dto.deviceUnit.DeviceUnitImportDTO;
import com.hebei.systemdemo.domain.po.DeviceUnit;

import java.util.List;

public interface IDeviceUnitService {
    Result getDeviceUnitListByProductionLineId(Long productionLineId);

    Result addDeviceUnit(DeviceUnit deviceUnit);

    Result batchAddDeviceUnit(DeviceUnitImportDTO deviceUnitImportDTO, Long userId);

    Result updateDeviceUnit(DeviceUnit deviceUnit);

    Result page(Integer current, Integer size, Long productionLineId, String unitCode, String unitName);

    Result batchDeleteDeviceUnit(List<Long> ids);
}
