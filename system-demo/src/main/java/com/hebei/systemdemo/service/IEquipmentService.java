package com.hebei.systemdemo.service;

import com.hebei.systemdemo.DTO.Result;
import com.hebei.systemdemo.pojo.Equipment;

import java.util.List;

public interface IEquipmentService {
    Result page(Integer current, Integer size, String unitCode, String equipmentCode, String equipmentName);

    Result getById(Long id);

    Result add(Equipment equipment);

    Result update(Equipment equipment);

    Result deleteById(Long id);

    Result deleteBatch(List<Long> ids);

    Result pageByLineCode(Integer current, Integer size, String lineCode);
}
