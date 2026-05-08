package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.WarehouseLocation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WarehouseLocationMapper {
    List<WarehouseLocation> page(@Param("productionLineId") Long productionLineId,
                                 @Param("areaCode") String areaCode,
                                 @Param("locationCode") String locationCode,
                                 @Param("keeperUsername") String keeperUsername,
                                 @Param("keeperRealName") String keeperRealName,
                                 @Param("creatorId") Long creatorId);

    List<WarehouseLocation> list(@Param("productionLineId") Long productionLineId,
                                 @Param("creatorId") Long creatorId);

    List<WarehouseLocation> listBySameCode(@Param("productionLineId") Long productionLineId,
                                           @Param("id") Long id,
                                           @Param("areaCode") String areaCode,
                                           @Param("locationCode") String locationCode,
                                           @Param("creatorId") Long creatorId);

    WarehouseLocation getById(@Param("id") Long id,
                              @Param("creatorId") Long creatorId);

    int add(WarehouseLocation warehouseLocation);

    int updateById(WarehouseLocation warehouseLocation);

    int batchDeleteByIds(@Param("ids") List<Long> ids);
}
