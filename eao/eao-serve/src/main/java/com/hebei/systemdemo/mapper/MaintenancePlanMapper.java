package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.MaintenancePlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenancePlanMapper {
    List<MaintenancePlan> page(@Param("planCode") String planCode,
                               @Param("productionLineCode") String productionLineCode,
                               @Param("maintenanceCategory") String maintenanceCategory,
                               @Param("status") String status,
                               @Param("excludeStatus") String excludeStatus,
                               @Param("creatorId") Long creatorId);

    MaintenancePlan getById(@Param("id") Long id,
                            @Param("creatorId") Long creatorId);

    int add(MaintenancePlan maintenancePlan);

    int updateById(MaintenancePlan maintenancePlan);

    int deleteByIds(@Param("ids") List<Long> ids,
                    @Param("updatedAt") String updatedAt,
                    @Param("allowedStatus") String allowedStatus,
                    @Param("creatorId") Long creatorId);

    int updateStatusByIds(@Param("ids") List<Long> ids,
                          @Param("fromStatus") String fromStatus,
                          @Param("toStatus") String toStatus,
                          @Param("updatedAt") String updatedAt,
                          @Param("creatorId") Long creatorId);
}
