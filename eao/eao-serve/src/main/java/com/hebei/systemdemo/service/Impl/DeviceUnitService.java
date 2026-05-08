package com.hebei.systemdemo.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.dto.deviceUnit.DeviceUnitImportDTO;
import com.hebei.systemdemo.domain.po.DeviceUnit;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.DeviceUnitMapper;
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
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        List<DeviceUnit> deviceUnitListByProductionLineId = deviceUnitMapper.getDeviceUnitListByProductionLineId(productionLineId, currentUserId());
        if (deviceUnitListByProductionLineId == null) {
            return Result.fail("未找到该产线下的设备单元");
        }
        return Result.ok(deviceUnitListByProductionLineId);
    }

    @Override
    public Result addDeviceUnit(DeviceUnit deviceUnit) {
        deviceUnit.setCreateAt(normalizeDateTime(deviceUnit.getCreateAt()));
        deviceUnit.setUpdateAt(normalizeDateTime(deviceUnit.getUpdateAt()));

        if (currentUserId() != null && deviceUnitMapper.getByUnitCodeAndCreatorId(deviceUnit.getUnitCode(), currentUserId()) != null) {
            return Result.fail("设备单元编码已存在");
        }

        int rows = deviceUnitMapper.addDeviceUnit(deviceUnit);
        if (rows <= 0 || deviceUnit.getId() == null) {
            return Result.fail("新增设备单元失败");
        }
        return Result.ok(Collections.singletonMap("id", deviceUnit.getId()));
    }

    @Override
    public Result batchAddDeviceUnit(DeviceUnitImportDTO deviceUnitImportDTO, Long userId) {
        if (deviceUnitImportDTO.getDeviceUnitAddDTOList() == null || deviceUnitImportDTO.getDeviceUnitAddDTOList().isEmpty()) {
            return Result.fail("导入数据不能为空");
        }
        deviceUnitImportDTO.getDeviceUnitAddDTOList().forEach(deviceUnitAddDTO -> {
            DeviceUnit deviceUnit = BeanUtil.copyProperties(deviceUnitAddDTO, DeviceUnit.class);
            deviceUnit.setCreateAt(normalizeDateTime(deviceUnit.getCreateAt()));
            deviceUnit.setUpdateAt(normalizeDateTime(deviceUnit.getUpdateAt()));
            deviceUnit.setCreatorId(userId);
            if (deviceUnitMapper.getByUnitCodeAndCreatorId(deviceUnit.getUnitCode(), userId) != null) {
                return;
            }
            deviceUnitMapper.addDeviceUnit(deviceUnit);
        });
        return Result.ok("成功导入" + deviceUnitImportDTO.getDeviceUnitAddDTOList().size() + "条数据");
    }

    @Override
    public Result updateDeviceUnit(DeviceUnit deviceUnit) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (deviceUnit.getId() == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "设备单元ID不能为空");
        }

        DeviceUnit existUnit = deviceUnitMapper.getById(deviceUnit.getId(), currentUserId());
        if (existUnit == null) {
            return Result.fail(ResultCode.NOT_FOUND, "设备单元不存在或无权限修改");
        }

        if (deviceUnitMapper.getByUnitCodeAndCreatorId(deviceUnit.getUnitCode(), currentUserId()) != null
                && !existUnit.getUnitCode().equals(deviceUnit.getUnitCode())) {
            return Result.fail("设备单元编码已存在");
        }

        deviceUnit.setUpdateAt(normalizeDateTime(deviceUnit.getUpdateAt()));
        int rows = deviceUnitMapper.updateDeviceUnit(deviceUnit);
        if (rows <= 0) {
            return Result.fail("更新设备单元失败");
        }

        return Result.ok("更新成功", null);
    }

    @Override
    public Result page(Integer current, Integer size, Long productionLineId, String unitCode, String unitName) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (current == null || current < 1) {
            return Result.fail("当前页码必须大于等于1");
        }
        if (size == null || size < 1) {
            return Result.fail("每页条数必须大于等于1");
        }

        String normalizedUnitCode = StringUtils.hasText(unitCode) ? unitCode.trim() : null;
        String normalizedUnitName = StringUtils.hasText(unitName) ? unitName.trim() : null;

        Page<DeviceUnit> page = PageHelper.startPage(current, size);
        List<DeviceUnit> records = deviceUnitMapper.page(productionLineId, normalizedUnitCode, normalizedUnitName, currentUserId());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result batchDeleteDeviceUnit(List<Long> ids) {
        if (currentUserId() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");
        }
        for (Long id : ids) {
            DeviceUnit exist = deviceUnitMapper.getById(id, currentUserId());
            if (exist == null) {
                return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的设备单元");
            }
        }

        int rows = deviceUnitMapper.batchDeleteByIds(ids);
        if (rows <= 0) {
            return Result.fail("删除设备单元失败");
        }

        return Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
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
