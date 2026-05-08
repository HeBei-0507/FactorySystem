package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.dto.lubricationStandard.LubricationStandardImportDTO;
import com.hebei.systemdemo.domain.po.LubricationStandard;

public interface ILubricationStandardService {
    Result page(Integer current, Integer size, String standardCode, String partCode, String partName,
                String feedPoint, String oilModels, String profession, String oilFeedType);

    Result getById(Long id);

    Result add(LubricationStandard lubricationStandard);

    Result batchAdd(LubricationStandardImportDTO importDTO, Long userId);

    Result update(LubricationStandard lubricationStandard);

    Result deleteById(Long id);

    Result getStandardListByPartCode(String partCode);
}
