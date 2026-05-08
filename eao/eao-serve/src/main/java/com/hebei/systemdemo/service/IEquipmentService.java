package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.dto.equipment.EquipmentImportDTO;
import com.hebei.systemdemo.domain.po.Equipment;

import java.util.List;

public interface IEquipmentService {
    Result page(Integer current, Integer size, Long deviceUnitId, String equipmentCode, String equipmentName,
                String equipmentCategory, String equipmentImportance, String repairStrategy,
                String overhaulTeam, String actTeam);

    Result getById(Long id);

    Result add(Equipment equipment);

    Result batchAdd(EquipmentImportDTO equipmentImportDTO, Long userId);

    Result update(Equipment equipment);

    Result deleteById(Long id);

    Result deleteBatch(List<Long> ids);

    Result getEquipmentListByDeviceUnitId(Long deviceUnitId);

    Result pageByLineCode(Integer current, Integer size, String lineCode, String equipmentCode, String equipmentName,
                          String equipmentCategory, String equipmentImportance, String repairStrategy,
                          String overhaulTeam, String actTeam);
}
