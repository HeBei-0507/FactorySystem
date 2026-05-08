package com.hebei.systemdemo.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.domain.dto.equipment.EquipmentImportDTO;
import com.hebei.systemdemo.domain.po.DeviceUnit;
import com.hebei.systemdemo.domain.po.Equipment;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.DeviceUnitMapper;
import com.hebei.systemdemo.mapper.EquipmentMapper;
import com.hebei.systemdemo.service.IEquipmentService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EquipmentService implements IEquipmentService {
    @Autowired private EquipmentMapper equipmentMapper;
    @Autowired private DeviceUnitMapper deviceUnitMapper;
    @Autowired private Validator validator;

    @Override
    public Result page(Integer current, Integer size, Long deviceUnitId, String equipmentCode, String equipmentName,
                       String equipmentCategory, String equipmentImportance, String repairStrategy,
                       String overhaulTeam, String actTeam) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<Equipment> page = PageHelper.startPage(current, size);
        List<Equipment> records = equipmentMapper.page(deviceUnitId, trim(equipmentCode), trim(equipmentName), trim(equipmentCategory), trim(equipmentImportance), trim(repairStrategy), trim(overhaulTeam), trim(actTeam), currentUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result getById(Long id) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        Equipment equipment = equipmentMapper.getById(id, currentUserId());
        return equipment == null ? Result.fail("未找到设备信息或无权限访问") : Result.ok(equipment);
    }

    @Override
    public Result add(Equipment equipment) {
        DeviceUnit unit = resolveDeviceUnitForEquipment(equipment, currentUserId());
        if (unit == null) return Result.fail("设备单元不存在或无权限使用");
        if (equipmentMapper.getByEquipmentCodeAndCreatorId(trim(equipment.getEquipmentCode()), currentUserId()) != null) return Result.fail("设备编码已存在");
        equipment.setDeviceUnitId(unit.getId());
        equipment.setUnitCode(unit.getUnitCode());
        equipment.setUnitName(unit.getUnitName());
        equipment.setCreatorId(currentUserId());
        equipment.setCreatedAt(normalizeDateTime(equipment.getCreatedAt()));
        equipment.setUpdatedAt(normalizeDateTime(equipment.getUpdatedAt()));
        int rows = equipmentMapper.add(equipment);
        return rows > 0 && equipment.getId() != null ? Result.ok(Collections.singletonMap("id", equipment.getId())) : Result.fail("新增设备失败");
    }

    @Override
    public Result batchAdd(EquipmentImportDTO dto, Long userId) {
        if (dto == null || dto.getEquipmentAddDTOList() == null || dto.getEquipmentAddDTOList().isEmpty()) return Result.fail("导入数据不能为空");
        List<String> failed = new ArrayList<>();
        int success = 0;
        for (var addDTO : dto.getEquipmentAddDTOList()) {
            String equipmentCode = norm(addDTO.getEquipmentCode());
            try {
                var violations = validator.validate(addDTO);
                if (!violations.isEmpty()) {
                    String message = violations.stream().map(ConstraintViolation::getMessage).distinct().collect(Collectors.joining("，"));
                    failed.add("设备编码[" + (StringUtils.hasText(equipmentCode) ? equipmentCode : "为空") + "]：" + message + "，已跳过");
                    continue;
                }
                Equipment equipment = BeanUtil.copyProperties(addDTO, Equipment.class);
                equipmentCode = norm(equipment.getEquipmentCode());
                if (!StringUtils.hasText(equipmentCode) || !StringUtils.hasText(norm(equipment.getEquipmentName())) || !StringUtils.hasText(norm(equipment.getOverhaulTeam())) || !StringUtils.hasText(norm(equipment.getActTeam())) || !StringUtils.hasText(norm(equipment.getRepairStrategy())) || !StringUtils.hasText(norm(equipment.getEquipmentImportance())) || !StringUtils.hasText(norm(equipment.getEquipmentCategory()))) {
                    failed.add("设备编码[" + (StringUtils.hasText(equipmentCode) ? equipmentCode : "为空") + "]：存在必填属性为空，已跳过");
                    continue;
                }
                DeviceUnit unit = resolveDeviceUnitForEquipment(equipment, userId);
                if (unit == null) { failed.add("设备编码[" + equipmentCode + "]：设备单元不存在或无权限，已跳过"); continue; }
                if (StringUtils.hasText(equipment.getUnitCode()) && !norm(unit.getUnitCode()).equals(norm(equipment.getUnitCode()))) {
                    failed.add("设备编码[" + equipmentCode + "]：设备单元编码不匹配，已跳过");
                    continue;
                }
                if (StringUtils.hasText(equipment.getUnitName()) && !norm(unit.getUnitName()).equals(norm(equipment.getUnitName()))) {
                    failed.add("设备编码[" + equipmentCode + "]：设备单元名称不匹配，已跳过");
                    continue;
                }
                if (equipmentMapper.getByEquipmentCodeAndCreatorId(equipmentCode, userId) != null) { failed.add("设备编码[" + equipmentCode + "]：设备编码已存在，已跳过"); continue; }
                equipment.setDeviceUnitId(unit.getId());
                equipment.setUnitCode(unit.getUnitCode());
                equipment.setUnitName(unit.getUnitName());
                equipment.setCreatorId(userId);
                equipment.setCreatedAt(normalizeDateTime(equipment.getCreatedAt()));
                equipment.setUpdatedAt(normalizeDateTime(equipment.getUpdatedAt()));
                int rows = equipmentMapper.add(equipment);
                if (rows > 0 && equipment.getId() != null) success++; else failed.add("设备编码[" + equipmentCode + "]：新增失败");
            } catch (Exception e) {
                failed.add("设备编码[" + (StringUtils.hasText(equipmentCode) ? equipmentCode : "为空") + "]：异常 - " + e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", success);
        result.put("failedCount", failed.size());
        result.put("failedRecords", failed);
        return failed.isEmpty() ? Result.ok("批量新增成功，共" + success + "条", result) : Result.ok("部分成功，成功" + success + "条，失败" + failed.size() + "条", result);
    }

    @Override
    public Result update(Equipment equipment) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (equipment.getId() == null) return Result.fail("设备ID不能为空");
        Equipment exist = equipmentMapper.getById(equipment.getId(), currentUserId());
        if (exist == null) return Result.fail("设备不存在或无权限修改");
        if (equipment.getDeviceUnitId() != null || StringUtils.hasText(equipment.getUnitCode())) {
            DeviceUnit unit = resolveDeviceUnitForEquipment(equipment, currentUserId());
            if (unit == null) return Result.fail("设备单元不存在或无权限使用");
            equipment.setDeviceUnitId(unit.getId());
            equipment.setUnitCode(unit.getUnitCode());
            equipment.setUnitName(unit.getUnitName());
        }
        if (StringUtils.hasText(equipment.getEquipmentCode()) && equipmentMapper.getByEquipmentCodeAndCreatorId(trim(equipment.getEquipmentCode()), currentUserId()) != null && !exist.getEquipmentCode().equals(trim(equipment.getEquipmentCode()))) {
            return Result.fail("设备编码已存在");
        }
        equipment.setUpdatedAt(normalizeDateTime(equipment.getUpdatedAt()));
        return equipmentMapper.updateById(equipment) > 0 ? Result.ok() : Result.fail("更新设备失败");
    }

    @Override
    public Result deleteById(Long id) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (equipmentMapper.getById(id, currentUserId()) == null) return Result.fail("设备不存在或无权限删除");
        return equipmentMapper.deleteById(id) > 0 ? Result.ok() : Result.fail("删除设备失败");
    }

    @Override
    public Result deleteBatch(List<Long> ids) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail("设备ID列表不能为空");
        for (Long id : ids) {
            if (equipmentMapper.getById(id, currentUserId()) == null) return Result.fail("存在不存在或无权限的设备");
        }
        return equipmentMapper.deleteBatch(ids) > 0 ? Result.ok(Collections.singletonMap("deletedCount", ids.size())) : Result.fail("批量删除设备失败");
    }

    @Override
    public Result getEquipmentListByDeviceUnitId(Long deviceUnitId) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        DeviceUnit unit = deviceUnitMapper.getById(deviceUnitId, currentUserId());
        if (unit == null) return Result.ok(Collections.emptyList());
        List<Map<String, Object>> list = equipmentMapper.list(new Equipment().setDeviceUnitId(deviceUnitId).setCreatorId(currentUserId())).stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId());
            m.put("deviceUnitId", e.getDeviceUnitId());
            m.put("equipmentName", e.getEquipmentName());
            m.put("equipmentCode", e.getEquipmentCode());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(list);
    }

    @Override
    public Result pageByLineCode(Integer current, Integer size, String lineCode, String equipmentCode, String equipmentName,
                                 String equipmentCategory, String equipmentImportance, String repairStrategy,
                                 String overhaulTeam, String actTeam) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        if (!StringUtils.hasText(lineCode)) return Result.fail("生产线编码不能为空");
        Page<Equipment> page = PageHelper.startPage(current, size);
        List<Equipment> records = equipmentMapper.listByLineCode(lineCode.trim(), trim(equipmentCode), trim(equipmentName), trim(equipmentCategory), trim(equipmentImportance), trim(repairStrategy), trim(overhaulTeam), trim(actTeam), currentUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    private DeviceUnit resolveDeviceUnitForEquipment(Equipment equipment, Long userId) {
        if (equipment == null || userId == null) return null;
        if (equipment.getDeviceUnitId() != null) {
            return deviceUnitMapper.getById(equipment.getDeviceUnitId(), userId);
        }
        if (StringUtils.hasText(equipment.getUnitCode())) {
            return deviceUnitMapper.getByUnitCodeAndCreatorId(equipment.getUnitCode().trim(), userId);
        }
        return null;
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private String normalizeDateTime(String v) {
        if (!StringUtils.hasText(v)) return null;
        String t = v.trim();
        return t.length() == 10 ? t + " 00:00:00" : t;
    }

    private String normalizeText(String v) {
        return StringUtils.hasText(v) ? v.trim() : "";
    }

    private String norm(String v) {
        return normalizeText(v);
    }

    private String trim(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }
}
