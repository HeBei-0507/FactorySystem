package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.DailyInspectionTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DailyInspectionTableMapper {
    List<DailyInspectionTable> list(DailyInspectionTable dailyInspectionTable);

    int addDailyInspectionTable(DailyInspectionTable dailyInspectionTable);

    DailyInspectionTable getById(@Param("id") Long id,
                                 @Param("creatorId") Long creatorId);

    int updateDailyInspectionTable(DailyInspectionTable dailyInspectionTable);

    List<DailyInspectionTable> page(@Param("productionLineName") String productionLineName,
                                    @Param("tableName") String tableName,
                                    @Param("tableCode") String tableCode,
                                    @Param("shiftMode") String shiftMode,
                                    @Param("creatorId") Long creatorId);

    int batchDeleteByIds(@Param("ids") List<Long> ids);

    String getMaxTableCodeByCreatorId(@Param("creatorId") Long creatorId);
}
