package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.DTO.Result;
import com.hebei.systemdemo.mapper.EquipmentMapper;
import com.hebei.systemdemo.pojo.Equipment;
import com.hebei.systemdemo.service.IEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EquipmentService implements IEquipmentService {
    @Autowired
    private EquipmentMapper equipmentMapper;

    @Override
    public Result page(Integer current, Integer size, String unitCode, String equipmentCode, String equipmentName) {
        if (current == null || current < 1) {
            return Result.fail("当前页码必须大于等于1");
        }
        if (size == null || size < 1) {
            return Result.fail("每页条数必须大于等于1");
        }

        String normalizedUnitCode = StringUtils.hasText(unitCode) ? unitCode.trim() : null;
        String normalizedEquipmentCode = StringUtils.hasText(equipmentCode) ? equipmentCode.trim() : null;
        String normalizedEquipmentName = StringUtils.hasText(equipmentName) ? equipmentName.trim() : null;

        Page<Equipment> page = PageHelper.startPage(current, size);
        List<Equipment> records = equipmentMapper.page(normalizedUnitCode, normalizedEquipmentCode, normalizedEquipmentName);

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result getById(Long id) {
        Equipment equipment = equipmentMapper.getById(id);
        if (equipment == null) {
            return Result.fail("未找到设备信息");
        }
        return Result.ok(equipment);
    }

    @Override
    public Result add(Equipment equipment) {
        equipment.setCreatedAt(normalizeDateTime(equipment.getCreatedAt()));
        equipment.setUpdatedAt(normalizeDateTime(equipment.getUpdatedAt()));
        int rows = equipmentMapper.add(equipment);
        if (rows <= 0 || equipment.getId() == null) {
            return Result.fail("新增设备失败");
        }
        return Result.ok(Collections.singletonMap("id", equipment.getId()));
    }

    @Override
    public Result update(Equipment equipment) {
        if (equipment.getId() == null) {
            return Result.fail("设备ID不能为空");
        }
        equipment.setCreatedAt(normalizeDateTime(equipment.getCreatedAt()));
        equipment.setUpdatedAt(normalizeDateTime(equipment.getUpdatedAt()));
        int rows = equipmentMapper.updateById(equipment);
        if (rows <= 0) {
            return Result.fail("更新设备失败");
        }
        return Result.ok();
    }

    @Override
    public Result deleteById(Long id) {
        int rows = equipmentMapper.deleteById(id);
        if (rows <= 0) {
            return Result.fail("删除设备失败");
        }
        return Result.ok();
    }

    @Override
    public Result deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("设备ID列表不能为空");
        }
        int rows = equipmentMapper.deleteBatch(ids);
        if (rows <= 0) {
            return Result.fail("批量删除设备失败");
        }
        return Result.ok(Collections.singletonMap("deletedCount", rows));
    }

    @Override
    public Result pageByLineCode(Integer current, Integer size, String lineCode) {
        if (current == null || current < 1) {
            return Result.fail("当前页码必须大于等于1");
        }
        if (size == null || size < 1) {
            return Result.fail("每页条数必须大于等于1");
        }
        if (!StringUtils.hasText(lineCode)) {
            return Result.fail("生产线编码不能为空");
        }

        Page<Equipment> page = PageHelper.startPage(current, size);
        List<Equipment> records = equipmentMapper.listByLineCode(lineCode.trim());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    private String normalizeDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() == 10) {
            return trimmed + " 00:00:00";
        }
        return trimmed;
    }
}
