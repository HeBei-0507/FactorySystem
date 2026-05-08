package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.MaintenanceStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenanceStandardMapper {
    List<MaintenanceStandard> page(@Param("standardCode") String standardCode,
                                   @Param("partCode") String partCode,
                                   @Param("partName") String partName,
                                   @Param("itemName") String itemName,
                                   @Param("profession") String profession,
                                   @Param("maintenanceCategory") String maintenanceCategory,
                                   @Param("maintenanceStartTime") String maintenanceStartTime,
                                   @Param("maintenanceEndTime") String maintenanceEndTime,
                                   @Param("workCategory") String workCategory,
                                   @Param("workContent") String workContent,
                                   @Param("partCodes") List<String> partCodes,
                                   @Param("maintainerId") Long maintainerId);

    MaintenanceStandard getById(@Param("id") Long id,
                                @Param("maintainerId") Long maintainerId);

    int add(MaintenanceStandard maintenanceStandard);

    int updateById(MaintenanceStandard maintenanceStandard);

    int deleteById(@Param("id") Long id,
                   @Param("maintainerId") Long maintainerId);
}
