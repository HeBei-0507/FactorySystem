package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.DeviceUnit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceUnitMapper {

    List<DeviceUnit> getDeviceUnitListByProductionLineId(@Param("productionLineId") Long productionLineId,
                                                         @Param("creatorId") Long creatorId);
    List<DeviceUnit> list(DeviceUnit deviceUnit);

    int addDeviceUnit(DeviceUnit deviceUnit);

    DeviceUnit getById(@Param("id") Long id,
                       @Param("creatorId") Long creatorId);

    int updateDeviceUnit(DeviceUnit deviceUnit);

    List<DeviceUnit> page(@Param("productionLineId") Long productionLineId,
                          @Param("unitCode") String unitCode,
                          @Param("unitName") String unitName,
                          @Param("creatorId") Long creatorId);

    int batchDeleteByIds(@Param("ids") List<Long> ids);

    DeviceUnit getByUnitCodeAndCreatorId(@Param("unitCode") String unitCode,
                                         @Param("creatorId") Long creatorId);
}
