package com.hebei.systemdemo.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.domain.dto.equipmentPart.EquipmentPartImportDTO;
import com.hebei.systemdemo.domain.po.Equipment;
import com.hebei.systemdemo.domain.po.EquipmentPart;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.EquipmentMapper;
import com.hebei.systemdemo.mapper.EquipmentPartMapper;
import com.hebei.systemdemo.service.IEquipmentPartService;
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
public class EquipmentPartService implements IEquipmentPartService {
    @Autowired private EquipmentPartMapper equipmentPartMapper;
    @Autowired private EquipmentMapper equipmentMapper;

    @Override
    public Result page(Integer current, Integer size, Long equipmentId, String partCode, String partName, String maintainer, String partType, String repairStrategy, String importanceLevel, String repairTeam, String operateTeam) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<EquipmentPart> page = PageHelper.startPage(current, size);
        List<EquipmentPart> records = equipmentPartMapper.page(equipmentId, trim(partCode), trim(partName), trim(maintainer), trim(partType), trim(repairStrategy), trim(importanceLevel), trim(repairTeam), trim(operateTeam), currentUserId());
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
        EquipmentPart equipmentPart = equipmentPartMapper.getById(id, currentUserId());
        return equipmentPart == null ? Result.fail("未找到设备部位信息或无权限访问") : Result.ok(equipmentPart);
    }

    @Override
    public Result add(EquipmentPart equipmentPart) {
        Equipment matchedEquipment = resolveEquipmentForPart(equipmentPart, currentUserId());
        if (matchedEquipment == null) return Result.fail("设备不存在或无权限使用该设备");
        equipmentPart.setEquipmentId(matchedEquipment.getId());
        equipmentPart.setDeviceCode(matchedEquipment.getEquipmentCode());
        equipmentPart.setDeviceName(matchedEquipment.getEquipmentName());
        if (equipmentPartMapper.getByEquipmentIdAndPartCodeAndCreatorId(equipmentPart.getEquipmentId(), trim(equipmentPart.getPartCode()), currentUserId()) != null) return Result.fail("当前设备下该设备部位编码已存在");
        equipmentPart.setCreatorId(currentUserId());
        equipmentPart.setCreatedAt(normalizeDateTime(equipmentPart.getCreatedAt()));
        equipmentPart.setUpdatedAt(normalizeDateTime(equipmentPart.getUpdatedAt()));
        int rows = equipmentPartMapper.add(equipmentPart);
        return rows > 0 && equipmentPart.getId() != null ? Result.ok(Collections.singletonMap("id", equipmentPart.getId())) : Result.fail("新增设备部位失败");
    }

    @Override
    public Result batchAdd(EquipmentPartImportDTO importDTO, Long userId) {
        if (importDTO == null || importDTO.getEquipmentPartList() == null || importDTO.getEquipmentPartList().isEmpty()) return Result.fail("导入数据不能为空");
        List<String> failed = new ArrayList<>();
        int success = 0;
        for (var dto : importDTO.getEquipmentPartList()) {
            try {
                EquipmentPart equipmentPart = BeanUtil.copyProperties(dto, EquipmentPart.class);
                String partCode = trimToEmpty(equipmentPart.getPartCode());
                String partName = trimToEmpty(equipmentPart.getPartName());
                if (!StringUtils.hasText(partCode) || !StringUtils.hasText(partName) || !StringUtils.hasText(equipmentPart.getRepairTeam()) || !StringUtils.hasText(equipmentPart.getOperateTeam()) || !StringUtils.hasText(equipmentPart.getRepairStrategy()) || !StringUtils.hasText(equipmentPart.getImportanceLevel()) || !StringUtils.hasText(equipmentPart.getPartType())) {
                    failed.add("设备部位编码[" + (StringUtils.hasText(partCode) ? partCode : "为空") + "]：存在必填属性为空，已跳过");
                    continue;
                }
                Equipment matched = resolveEquipmentForPart(equipmentPart, userId);
                if (matched == null) { failed.add("设备部位编码[" + partCode + "]：设备不存在或无权限，已跳过"); continue; }
                if (StringUtils.hasText(equipmentPart.getDeviceCode()) && !trimToEmpty(equipmentPart.getDeviceCode()).equals(trimToEmpty(matched.getEquipmentCode()))) {
                    failed.add("设备部位编码[" + partCode + "]：设备编码不匹配，已跳过");
                    continue;
                }
                if (StringUtils.hasText(equipmentPart.getDeviceName()) && !trimToEmpty(equipmentPart.getDeviceName()).equals(trimToEmpty(matched.getEquipmentName()))) {
                    failed.add("设备部位编码[" + partCode + "]：设备名称不匹配，已跳过");
                    continue;
                }
                equipmentPart.setEquipmentId(matched.getId());
                equipmentPart.setDeviceCode(matched.getEquipmentCode());
                equipmentPart.setDeviceName(matched.getEquipmentName());
                equipmentPart.setPartCode(partCode);
                equipmentPart.setPartName(partName);
                equipmentPart.setCreatorId(userId);
                if (equipmentPartMapper.getByEquipmentIdAndPartCodeAndCreatorId(equipmentPart.getEquipmentId(), partCode, userId) != null) { failed.add("设备[" + matched.getEquipmentCode() + "]下的设备部位编码[" + partCode + "]已存在"); continue; }
                equipmentPart.setCreatedAt(normalizeDateTime(equipmentPart.getCreatedAt()));
                equipmentPart.setUpdatedAt(normalizeDateTime(equipmentPart.getUpdatedAt()));
                int rows = equipmentPartMapper.add(equipmentPart);
                if (rows > 0 && equipmentPart.getId() != null) success++; else failed.add("设备[" + matched.getEquipmentCode() + "]下的设备部位编码[" + partCode + "]：新增失败");
            } catch (Exception e) {
                failed.add("设备部位导入异常：" + e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", success);
        result.put("failedCount", failed.size());
        result.put("failedRecords", failed);
        return failed.isEmpty() ? Result.ok("批量新增成功，共" + success + "条", result) : Result.ok("部分成功，成功" + success + "条，失败" + failed.size() + "条", result);
    }

    @Override
    public Result update(EquipmentPart equipmentPart) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (equipmentPart.getId() == null) return Result.fail("设备部位ID不能为空");
        EquipmentPart existPart = equipmentPartMapper.getById(equipmentPart.getId(), currentUserId());
        if (existPart == null) return Result.fail("设备部位不存在或无权限修改");
        Equipment matched = resolveEquipmentForPart(equipmentPart.getEquipmentId() != null || StringUtils.hasText(equipmentPart.getDeviceCode()) ? equipmentPart : new EquipmentPart().setEquipmentId(existPart.getEquipmentId()).setDeviceCode(existPart.getDeviceCode()), currentUserId());
        if (matched == null) return Result.fail("设备不存在或无权限使用");
        Long nextEquipmentId = matched.getId();
        String nextPartCode = StringUtils.hasText(equipmentPart.getPartCode()) ? equipmentPart.getPartCode().trim() : existPart.getPartCode();
        EquipmentPart duplicated = equipmentPartMapper.getByEquipmentIdAndPartCodeAndCreatorId(nextEquipmentId, nextPartCode, currentUserId());
        if (duplicated != null && !duplicated.getId().equals(existPart.getId())) return Result.fail("当前设备下该设备部位编码已存在");
        equipmentPart.setEquipmentId(nextEquipmentId);
        equipmentPart.setDeviceCode(matched.getEquipmentCode());
        equipmentPart.setDeviceName(matched.getEquipmentName());
        if (StringUtils.hasText(equipmentPart.getPartCode())) equipmentPart.setPartCode(nextPartCode);
        equipmentPart.setUpdatedAt(normalizeDateTime(equipmentPart.getUpdatedAt()));
        return equipmentPartMapper.updateById(equipmentPart) > 0 ? Result.ok() : Result.fail("更新设备部位失败");
    }

    @Override
    public Result deleteById(Long id) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (equipmentPartMapper.getById(id, currentUserId()) == null) return Result.fail("设备部位不存在或无权限删除");
        return equipmentPartMapper.deleteById(id) > 0 ? Result.ok() : Result.fail("删除设备部位失败");
    }

    @Override
    public Result getPartListByEquipmentId(Long equipmentId) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        Equipment matched = equipmentMapper.getById(equipmentId, currentUserId());
        if (matched == null) return Result.ok(Collections.emptyList());
        List<Map<String, Object>> list = equipmentPartMapper.list(new EquipmentPart().setEquipmentId(equipmentId).setCreatorId(currentUserId())).stream().map(part -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", part.getId());
            m.put("equipmentId", part.getEquipmentId());
            m.put("partName", part.getPartName());
            m.put("partCode", part.getPartCode());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(list);
    }

    private Equipment resolveEquipmentForPart(EquipmentPart equipmentPart, Long userId) {
        if (equipmentPart == null || userId == null) return null;
        if (equipmentPart.getEquipmentId() != null) {
            return equipmentMapper.getById(equipmentPart.getEquipmentId(), userId);
        }
        if (StringUtils.hasText(equipmentPart.getDeviceCode())) {
            return equipmentMapper.list(new Equipment().setEquipmentCode(equipmentPart.getDeviceCode().trim()).setCreatorId(userId)).stream().findFirst().orElse(null);
        }
        return null;
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private String normalizeDateTime(String value) {
        if (!StringUtils.hasText(value)) return null;
        String trimmed = value.trim();
        return trimmed.length() == 10 ? trimmed + " 00:00:00" : trimmed;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String trimToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
