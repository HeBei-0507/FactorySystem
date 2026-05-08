package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.DailyInspectionTable;

import java.util.List;

public interface IDailyInspectionTableService {
    Result addDailyInspectionTable(DailyInspectionTable dailyInspectionTable);

    Result updateDailyInspectionTable(DailyInspectionTable dailyInspectionTable);

    Result page(Integer current, Integer size, String productionLineName, String tableName, String tableCode, String shiftMode);

    Result batchDeleteDailyInspectionTable(List<Long> ids);
}
