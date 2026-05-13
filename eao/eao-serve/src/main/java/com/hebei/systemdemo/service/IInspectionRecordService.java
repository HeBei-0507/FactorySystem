package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.InspectionRecord;

import java.util.List;

public interface IInspectionRecordService {
    Result page(Integer current, Integer size, Long productionLineId, Long routeId,
                String planDateStart, String planDateEnd, String completionFlag,
                String partCode, String partName);

    Result getById(Long id);

    Result add(InspectionRecord inspectionRecord);

    Result update(InspectionRecord inspectionRecord);

    Result deleteById(Long id);

    Result batchDelete(List<Long> ids);

    Result complete(List<Long> ids);

    void createPendingRecordFromStandard(InspectionRecord inspectionRecord);
}
