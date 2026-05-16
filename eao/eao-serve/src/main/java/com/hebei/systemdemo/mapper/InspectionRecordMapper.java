package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.InspectionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InspectionRecordMapper {
    int add(InspectionRecord inspectionRecord);

    int updateById(@Param("record") InspectionRecord inspectionRecord,
                   @Param("creatorId") Long creatorId);

    InspectionRecord getById(@Param("id") Long id,
                             @Param("creatorId") Long creatorId);

    List<InspectionRecord> page(@Param("productionLineId") Long productionLineId,
                                @Param("routeId") Long routeId,
                                @Param("planDateStart") String planDateStart,
                                @Param("planDateEnd") String planDateEnd,
                                @Param("completionFlag") String completionFlag,
                                @Param("partCode") String partCode,
                                @Param("partName") String partName,
                                @Param("creatorId") Long creatorId);

    List<InspectionRecord> planSummaryPage(@Param("productionLineId") Long productionLineId,
                                           @Param("routeName") String routeName,
                                           @Param("planSource") String planSource,
                                           @Param("creatorId") Long creatorId);

    int deleteById(@Param("id") Long id,
                   @Param("creatorId") Long creatorId);

    int batchDeleteByIds(@Param("ids") List<Long> ids,
                         @Param("creatorId") Long creatorId);

    List<InspectionRecord> listByInspectionStandardId(@Param("inspectionStandardId") Long inspectionStandardId,
                                                      @Param("creatorId") Long creatorId);
}
