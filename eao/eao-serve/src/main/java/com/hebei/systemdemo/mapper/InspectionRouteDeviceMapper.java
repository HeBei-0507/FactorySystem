package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.InspectionRouteDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InspectionRouteDeviceMapper {
    List<InspectionRouteDevice> getInspectionRouteDeviceListByRouteCode(@Param("routeCode") String routeCode,
                                                                        @Param("creatorId") Long creatorId);

    List<InspectionRouteDevice> list(InspectionRouteDevice inspectionRouteDevice);

    int addInspectionRouteDevice(InspectionRouteDevice inspectionRouteDevice);

    InspectionRouteDevice getById(@Param("id") Long id,
                                  @Param("creatorId") Long creatorId);

    int updateInspectionRouteDevice(InspectionRouteDevice inspectionRouteDevice);

    List<InspectionRouteDevice> page(@Param("routeCode") String routeCode,
                                     @Param("equipmentCode") String equipmentCode,
                                     @Param("equipmentName") String equipmentName,
                                     @Param("creatorId") Long creatorId);

    int batchDeleteByIds(@Param("ids") List<Long> ids);
}
