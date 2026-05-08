package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.MaintenancePlan;

import java.util.List;

public interface IMaintenancePlanService {
    Result page(Integer current, Integer size, String planCode, String productionLineCode,
                String maintenanceCategory, String status, Boolean excludePending);

    Result getById(Long id);

    Result add(MaintenancePlan maintenancePlan);

    Result update(MaintenancePlan maintenancePlan);

    Result deleteByIds(List<Long> ids);

    Result submitByIds(List<Long> ids);

    Result approveByIds(List<Long> ids);

    Result rollbackApproveByIds(List<Long> ids);
}
