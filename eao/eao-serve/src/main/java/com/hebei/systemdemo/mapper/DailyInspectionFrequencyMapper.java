package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.DailyInspectionFrequency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DailyInspectionFrequencyMapper {
    List<DailyInspectionFrequency> list(DailyInspectionFrequency dailyInspectionFrequency);

    int addDailyInspectionFrequency(DailyInspectionFrequency dailyInspectionFrequency);

    DailyInspectionFrequency getById(@Param("id") Long id,
                                     @Param("creatorId") Long creatorId);

    int updateDailyInspectionFrequency(DailyInspectionFrequency dailyInspectionFrequency);

    List<DailyInspectionFrequency> page(@Param("dailyInspectionTableId") Long dailyInspectionTableId,
                                        @Param("frequencyCode") String frequencyCode,
                                        @Param("areaDeviceName") String areaDeviceName,
                                        @Param("creatorId") Long creatorId);

    int batchDeleteByIds(@Param("ids") List<Long> ids);
}
