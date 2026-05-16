package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.LubricationStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LubricationStandardMapper {

    List<LubricationStandard> page(@Param("productionLineId") Long productionLineId,
                                   @Param("deviceUnitId") Long deviceUnitId,
                                   @Param("equipmentId") Long equipmentId,
                                   @Param("standardCode") String standardCode,
                                   @Param("partCode") String partCode,
                                   @Param("partName") String partName,
                                   @Param("feedPoint") String feedPoint,
                                   @Param("oilModels") String oilModels,
                                   @Param("profession") String profession,
                                   @Param("oilFeedType") String oilFeedType,
                                   @Param("creatorId") Long creatorId);

    List<LubricationStandard> list(LubricationStandard lubricationStandard);

    LubricationStandard getById(@Param("id") Long id,
                                @Param("creatorId") Long creatorId);

    int add(LubricationStandard lubricationStandard);

    int updateById(@Param("record") LubricationStandard lubricationStandard,
                   @Param("creatorId") Long creatorId);

    int deleteById(@Param("id") Long id,
                   @Param("creatorId") Long creatorId);
}
