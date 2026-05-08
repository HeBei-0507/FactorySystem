package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.InspectionRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InspectionRouteMapper {
    List<InspectionRoute> getInspectionRouteListByProductionLineId(@Param("productionLineId") Long productionLineId,
                                                                   @Param("creatorId") Long creatorId);

    List<InspectionRoute> list(InspectionRoute inspectionRoute);

    int addInspectionRoute(InspectionRoute inspectionRoute);

    InspectionRoute getById(@Param("id") Long id,
                            @Param("creatorId") Long creatorId);

    int updateInspectionRoute(InspectionRoute inspectionRoute);

    List<InspectionRoute> page(@Param("productionLineId") Long productionLineId,
                               @Param("routeCode") String routeCode,
                               @Param("routeName") String routeName,
                               @Param("creatorId") Long creatorId);

    int batchDeleteByIds(@Param("ids") List<Long> ids);
}
