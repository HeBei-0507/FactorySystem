package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.InspectionRouteDevice;

import java.util.List;

public interface IInspectionRouteDeviceService {
    Result getInspectionRouteDeviceListByRouteCode(String routeCode);

    Result addInspectionRouteDevice(InspectionRouteDevice inspectionRouteDevice);

    Result updateInspectionRouteDevice(InspectionRouteDevice inspectionRouteDevice);

    Result page(Integer current, Integer size, String routeCode, String equipmentCode, String equipmentName);

    Result batchDeleteInspectionRouteDevice(List<Long> ids);
}
