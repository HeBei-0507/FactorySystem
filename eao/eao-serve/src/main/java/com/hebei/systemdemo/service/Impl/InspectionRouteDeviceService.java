package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.InspectionRouteDevice;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.InspectionRouteDeviceMapper;
import com.hebei.systemdemo.service.IInspectionRouteDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InspectionRouteDeviceService implements IInspectionRouteDeviceService {
    @Autowired
    private InspectionRouteDeviceMapper inspectionRouteDeviceMapper;

    @Override
    public Result getInspectionRouteDeviceListByRouteCode(String routeCode) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        List<InspectionRouteDevice> deviceList = inspectionRouteDeviceMapper.getInspectionRouteDeviceListByRouteCode(trimToNull(routeCode), currentUserId());
        return Result.ok(deviceList == null ? Collections.emptyList() : deviceList);
    }

    @Override
    public Result addInspectionRouteDevice(InspectionRouteDevice inspectionRouteDevice) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        inspectionRouteDevice.setCreatorId(currentUserId());
        inspectionRouteDevice.setCreatedAt(normalizeDateTime(inspectionRouteDevice.getCreatedAt()));
        inspectionRouteDevice.setUpdatedAt(normalizeDateTime(inspectionRouteDevice.getUpdatedAt()));

        int rows = inspectionRouteDeviceMapper.addInspectionRouteDevice(inspectionRouteDevice);
        if (rows <= 0 || inspectionRouteDevice.getId() == null) return Result.fail("新增路线点检设备失败");
        return Result.ok(Collections.singletonMap("id", inspectionRouteDevice.getId()));
    }

    @Override
    public Result updateInspectionRouteDevice(InspectionRouteDevice inspectionRouteDevice) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (inspectionRouteDevice.getId() == null) return Result.fail(ResultCode.BAD_REQUEST, "ID不能为空");

        InspectionRouteDevice existDevice = inspectionRouteDeviceMapper.getById(inspectionRouteDevice.getId(), currentUserId());
        if (existDevice == null) return Result.fail(ResultCode.NOT_FOUND, "路线点检设备不存在或无权限访问");

        inspectionRouteDevice.setUpdatedAt(normalizeDateTime(inspectionRouteDevice.getUpdatedAt()));
        int rows = inspectionRouteDeviceMapper.updateInspectionRouteDevice(inspectionRouteDevice);
        if (rows <= 0) return Result.fail("更新路线点检设备失败");

        return Result.ok("更新成功", null);
    }

    @Override
    public Result page(Integer current, Integer size, String routeCode, String equipmentCode, String equipmentName) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");

        String normalizedRouteCode = trimToNull(routeCode);
        String normalizedEquipmentCode = trimToNull(equipmentCode);
        String normalizedEquipmentName = trimToNull(equipmentName);

        Page<InspectionRouteDevice> page = PageHelper.startPage(current, size);
        List<InspectionRouteDevice> records = inspectionRouteDeviceMapper.page(normalizedRouteCode, normalizedEquipmentCode, normalizedEquipmentName, currentUserId());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result batchDeleteInspectionRouteDevice(List<Long> ids) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");

        List<Long> deletableIds = new ArrayList<>();
        for (Long id : ids) {
            InspectionRouteDevice existDevice = inspectionRouteDeviceMapper.getById(id, currentUserId());
            if (existDevice != null) deletableIds.add(id);
        }
        if (deletableIds.isEmpty()) return Result.fail("未找到可删除的路线点检设备或无权限删除");

        int rows = inspectionRouteDeviceMapper.batchDeleteByIds(deletableIds);
        if (rows <= 0) return Result.fail("删除路线点检设备失败");

        return Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeDateTime(String value) {
        if (!StringUtils.hasText(value)) return null;
        String trimmed = value.trim();
        return trimmed.length() == 10 ? trimmed + " 00:00:00" : trimmed;
    }
}
