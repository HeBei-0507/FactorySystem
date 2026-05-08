package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.dto.inspectionStandard.InspectionStandardImportDTO;
import com.hebei.systemdemo.domain.po.InspectionStandard;

import java.util.List;

public interface IInspectionStandardService {
    Result page(Integer current, Integer size, String inspectionName, String partCode, List<String> partCodes,
                String partName, String cycleUnit, String dataType, String signalType,
                String implementationMethod, String standardCategory, String unitOfMeasurement,
                String profession);

    Result getById(Long id);

    Result add(InspectionStandard inspectionStandard);

    Result batchAdd(InspectionStandardImportDTO importDTO, Long userId);

    Result update(InspectionStandard inspectionStandard);

    Result deleteById(Long id);

    Result getStandardListByPartCode(String partCode);
}
