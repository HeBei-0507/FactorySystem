package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.WarehouseLocation;

import java.util.List;

public interface IWarehouseLocationService {
    Result page(Integer current, Integer size, Long productionLineId, String areaCode, String locationCode, String keeperUsername, String keeperRealName);

    Result listByProductionLine(Long productionLineId);

    Result add(WarehouseLocation warehouseLocation, Long userId, String username, String realName);

    Result update(WarehouseLocation warehouseLocation);

    Result batchDelete(List<Long> ids);
}
