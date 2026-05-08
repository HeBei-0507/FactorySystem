package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.InspectionStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InspectionStandardMapper {
    List<InspectionStandard> list(InspectionStandard inspectionStandard);

    int add(InspectionStandard inspectionStandard);

    InspectionStandard getById(@Param("id") Long id,
                               @Param("creatorId") Long creatorId);

    int updateById(InspectionStandard inspectionStandard);

    int deleteById(@Param("id") Long id);

    List<InspectionStandard> page(@Param("inspectionName") String inspectionName,
                                  @Param("partCode") String partCode,
                                  @Param("partCodes") List<String> partCodes,
                                  @Param("partName") String partName,
                                  @Param("cycleUnit") String cycleUnit,
                                  @Param("dataType") String dataType,
                                  @Param("signalType") String signalType,
                                  @Param("implementationMethod") String implementationMethod,
                                  @Param("standardCategory") String standardCategory,
                                  @Param("unitOfMeasurement") String unitOfMeasurement,
                                  @Param("profession") String profession,
                                  @Param("creatorId") Long creatorId);
}
