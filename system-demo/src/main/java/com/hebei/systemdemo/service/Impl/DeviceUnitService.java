package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.DTO.Result;
import com.hebei.systemdemo.mapper.DeviceUnitMapper;
import com.hebei.systemdemo.pojo.DeviceUnit;
import com.hebei.systemdemo.service.IDeviceUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceUnitService implements IDeviceUnitService {
    @Autowired
    private DeviceUnitMapper deviceUnitMapper;

    @Override
    public Result getDeviceUnitListByProductionLineId(Long productionLineId) {
        List<DeviceUnit> deviceUnitListByProductionLineId = deviceUnitMapper.getDeviceUnitListByProductionLineId(productionLineId);
        if (deviceUnitListByProductionLineId == null) {
            return Result.fail("未找到该产线下的设备单元");
        }
        return Result.ok(deviceUnitListByProductionLineId);
    }

    @Override
    public Result addDeviceUnit(DeviceUnit deviceUnit) {
        deviceUnit.setCreateAt(normalizeDateTime(deviceUnit.getCreateAt()));
        deviceUnit.setUpdateAt(normalizeDateTime(deviceUnit.getUpdateAt()));

        int rows = deviceUnitMapper.addDeviceUnit(deviceUnit);
        if (rows <= 0 || deviceUnit.getId() == null) {
            return Result.fail("新增设备单元失败");
        }
        return Result.ok(Collections.singletonMap("id", deviceUnit.getId()));
    }

    @Override
    public Result page(Integer current, Integer size, Long productionLineId, String unitCode, String unitName) {
        if (current == null || current < 1) {
            return Result.fail("当前页码必须大于等于1");
        }
        if (size == null || size < 1) {
            return Result.fail("每页条数必须大于等于1");
        }

        String normalizedUnitCode = StringUtils.hasText(unitCode) ? unitCode.trim() : null;
        String normalizedUnitName = StringUtils.hasText(unitName) ? unitName.trim() : null;

        Page<DeviceUnit> page = PageHelper.startPage(current, size);
        List<DeviceUnit> records = deviceUnitMapper.page(productionLineId, normalizedUnitCode, normalizedUnitName);

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result updateDeviceUnit(DeviceUnit deviceUnit) {
        if (deviceUnit.getId() == null) {
            return Result.fail("设备单元ID不能为空");
        }
        deviceUnit.setUnitCode(StringUtils.hasText(deviceUnit.getUnitCode()) ? deviceUnit.getUnitCode().trim() : null);
        deviceUnit.setUnitName(StringUtils.hasText(deviceUnit.getUnitName()) ? deviceUnit.getUnitName().trim() : null);
        deviceUnit.setCreateAt(normalizeDateTime(deviceUnit.getCreateAt()));
        deviceUnit.setUpdateAt(normalizeDateTime(deviceUnit.getUpdateAt()));

        int rows = deviceUnitMapper.updateDeviceUnit(deviceUnit);
        if (rows <= 0) {
            return Result.fail("更新设备单元失败，记录不存在或未发生变化");
        }
        return Result.ok(Collections.singletonMap("id", deviceUnit.getId()));
    }

    @Override
    public Result deleteDeviceUnit(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("设备单元ID列表不能为空");
        }
        List<Long> validIds = ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (validIds.isEmpty()) {
            return Result.fail("设备单元ID列表不能为空");
        }

        int rows = deviceUnitMapper.deleteDeviceUnit(validIds);
        if (rows <= 0) {
            return Result.fail("删除设备单元失败，记录不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", rows);
        data.put("ids", validIds);
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
