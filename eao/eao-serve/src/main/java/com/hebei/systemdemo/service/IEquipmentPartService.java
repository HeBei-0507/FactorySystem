package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.dto.equipmentPart.EquipmentPartImportDTO;
import com.hebei.systemdemo.domain.po.EquipmentPart;

public interface IEquipmentPartService {
    Result page(Integer current, Integer size, Long equipmentId, String partCode, String partName,
                String maintainer, String partType, String repairStrategy, String importanceLevel,
                String repairTeam, String operateTeam);

    Result getById(Long id);

    Result add(EquipmentPart equipmentPart);

    Result batchAdd(EquipmentPartImportDTO importDTO, Long userId);

    Result update(EquipmentPart equipmentPart);

    Result deleteById(Long id);

    Result getPartListByEquipmentId(Long equipmentId);
}
