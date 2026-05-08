package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.InspectionRoute;

import java.util.List;

public interface IInspectionRouteService {
    Result getInspectionRouteListByProductionLineId(Long productionLineId);

    Result addInspectionRoute(InspectionRoute inspectionRoute);

    Result updateInspectionRoute(InspectionRoute inspectionRoute);

    Result page(Integer current, Integer size, Long productionLineId, String routeCode, String routeName);

    Result batchDeleteInspectionRoute(List<Long> ids);
}
