package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.DailyInspectionFrequency;

import java.util.List;

public interface IDailyInspectionFrequencyService {
    Result addDailyInspectionFrequency(DailyInspectionFrequency dailyInspectionFrequency);

    Result updateDailyInspectionFrequency(DailyInspectionFrequency dailyInspectionFrequency);

    Result page(Integer current, Integer size, Long dailyInspectionTableId, String frequencyCode, String areaDeviceName);

    Result batchDeleteDailyInspectionFrequency(List<Long> ids);
}
