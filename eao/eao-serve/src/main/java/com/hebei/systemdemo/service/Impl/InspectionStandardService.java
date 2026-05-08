package com.hebei.systemdemo.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.domain.dto.inspectionStandard.InspectionStandardImportDTO;
import com.hebei.systemdemo.domain.po.EquipmentPart;
import com.hebei.systemdemo.domain.po.IDAddress;
import com.hebei.systemdemo.domain.po.InspectionStandard;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.EquipmentPartMapper;
import com.hebei.systemdemo.mapper.IDAddressMapper;
import com.hebei.systemdemo.mapper.InspectionStandardMapper;
import com.hebei.systemdemo.service.IInspectionStandardService;
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
public class InspectionStandardService implements IInspectionStandardService {
    @Autowired
    private InspectionStandardMapper inspectionStandardMapper;
    @Autowired
    private EquipmentPartMapper equipmentPartMapper;
    @Autowired
    private IDAddressMapper idAddressMapper;

    @Override
    public Result page(Integer current, Integer size, String inspectionName, String partCode, List<String> partCodes,
                       String partName, String cycleUnit, String dataType, String signalType,
                       String implementationMethod, String standardCategory, String unitOfMeasurement,
                       String profession) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<InspectionStandard> page = PageHelper.startPage(current, size);
        List<InspectionStandard> records = inspectionStandardMapper.page(
                trimToNull(inspectionName), trimToNull(partCode), normalizePartCodes(partCodes), trimToNull(partName),
                trimToNull(cycleUnit), trimToNull(dataType), trimToNull(signalType), trimToNull(implementationMethod),
                trimToNull(standardCategory), trimToNull(unitOfMeasurement), trimToNull(profession), currentUserId());
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
        InspectionStandard standard = inspectionStandardMapper.getById(id, currentUserId());
        if (standard == null) return Result.fail("未找到点检标准信息或无权限访问");
        return Result.ok(standard);
    }

    @Override
    public Result add(InspectionStandard inspectionStandard) {
        if (!StringUtils.hasText(inspectionStandard.getPartCode())) return Result.fail("设备部位编码不能为空");
        EquipmentPart matchedPart = findEquipmentPartByCode(inspectionStandard.getPartCode().trim(), currentUserId());
        if (matchedPart == null) return Result.fail("设备部位编码[" + inspectionStandard.getPartCode().trim() + "]不存在或无权限使用，新增失败");
        inspectionStandard.setPartCode(trimToNull(matchedPart.getPartCode()));
        inspectionStandard.setPartName(resolveInspectionPartName(inspectionStandard.getPartName(), matchedPart));
        fillIdLocationFields(inspectionStandard);
        inspectionStandard.setCreatorId(currentUserId());
        inspectionStandard.setCreatedAt(normalizeDateTime(inspectionStandard.getCreatedAt()));
        inspectionStandard.setUpdatedAt(normalizeDateTime(inspectionStandard.getUpdatedAt()));
        int rows = inspectionStandardMapper.add(inspectionStandard);
        if (rows <= 0 || inspectionStandard.getId() == null) return Result.fail("新增点检标准失败");
        return Result.ok(Collections.singletonMap("id", inspectionStandard.getId()));
    }

    @Override
    public Result update(InspectionStandard inspectionStandard) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (inspectionStandard.getId() == null) return Result.fail("点检标准ID不能为空");
        InspectionStandard existStandard = inspectionStandardMapper.getById(inspectionStandard.getId(), currentUserId());
        if (existStandard == null) return Result.fail("点检标准不存在或无权限修改");
        if (StringUtils.hasText(inspectionStandard.getPartCode())) {
            EquipmentPart matchedPart = findEquipmentPartByCode(inspectionStandard.getPartCode().trim(), currentUserId());
            if (matchedPart == null) return Result.fail("设备部位编码不存在或无权限使用");
            inspectionStandard.setPartCode(trimToNull(matchedPart.getPartCode()));
            inspectionStandard.setPartName(resolveInspectionPartName(inspectionStandard.getPartName(), matchedPart));
        }
        fillIdLocationFields(inspectionStandard);
        inspectionStandard.setUpdatedAt(normalizeDateTime(inspectionStandard.getUpdatedAt()));
        int rows = inspectionStandardMapper.updateById(inspectionStandard);
        if (rows <= 0) return Result.fail("更新点检标准失败");
        return Result.ok();
    }

    @Override
    public Result deleteById(Long id) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        InspectionStandard existStandard = inspectionStandardMapper.getById(id, currentUserId());
        if (existStandard == null) return Result.fail("点检标准不存在或无权限删除");
        int rows = inspectionStandardMapper.deleteById(id);
        if (rows <= 0) return Result.fail("删除点检标准失败");
        return Result.ok();
    }

    @Override
    public Result getStandardListByPartCode(String partCode) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        EquipmentPart matchedPart = findEquipmentPartByCode(partCode, currentUserId());
        if (matchedPart == null) return Result.ok(Collections.emptyList());
        List<Map<String, Object>> list = inspectionStandardMapper.list(new InspectionStandard().setPartCode(trimToNull(partCode)).setCreatorId(currentUserId()))
                .stream().map(standard -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", standard.getId());
                    result.put("inspectionName", standard.getInspectionName());
                    return result;
                }).collect(Collectors.toList());
        return Result.ok(list);
    }

    @Override
    public Result batchAdd(InspectionStandardImportDTO importDTO, Long userId) {
        if (importDTO == null || importDTO.getInspectionStandardList() == null || importDTO.getInspectionStandardList().isEmpty()) return Result.fail("导入数据不能为空");
        List<String> failedRecords = new ArrayList<>();
        int successCount = 0;
        for (var standardAddDTO : importDTO.getInspectionStandardList()) {
            try {
                InspectionStandard inspectionStandard = BeanUtil.copyProperties(standardAddDTO, InspectionStandard.class);
                String inspectionName = trimToEmpty(inspectionStandard.getInspectionName());
                String partCode = trimToEmpty(inspectionStandard.getPartCode());
                if (!StringUtils.hasText(inspectionName)) {
                    failedRecords.add("点检标准名称[为空]：点检标准名称不能为空");
                    continue;
                }
                if (!StringUtils.hasText(partCode)) {
                    failedRecords.add("点检标准[" + inspectionName + "]：设备部位编码不能为空");
                    continue;
                }
                EquipmentPart matchedPart = findEquipmentPartByCode(partCode, userId);
                if (matchedPart == null) {
                    failedRecords.add("点检标准[" + inspectionName + "]：设备部位编码[" + partCode + "]不存在或无权限，已跳过");
                    continue;
                }
                inspectionStandard.setInspectionName(inspectionName);
                inspectionStandard.setPartCode(trimToNull(matchedPart.getPartCode()));
                inspectionStandard.setPartName(resolveInspectionPartName(inspectionStandard.getPartName(), matchedPart));
                fillIdLocationFields(inspectionStandard);
                inspectionStandard.setCreatorId(userId);
                inspectionStandard.setCreatedAt(normalizeDateTime(inspectionStandard.getCreatedAt()));
                inspectionStandard.setUpdatedAt(normalizeDateTime(inspectionStandard.getUpdatedAt()));
                int rows = inspectionStandardMapper.add(inspectionStandard);
                if (rows > 0 && inspectionStandard.getId() != null) successCount++; else failedRecords.add("点检标准[" + inspectionName + "]：新增失败");
            } catch (Exception e) {
                failedRecords.add("点检标准[" + standardAddDTO.getInspectionName() + "]：异常 - " + e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failedCount", failedRecords.size());
        result.put("failedRecords", failedRecords);
        return failedRecords.isEmpty() ? Result.ok("批量新增成功，共" + successCount + "条", result) : Result.ok("部分成功，成功" + successCount + "条，失败" + failedRecords.size() + "条", result);
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private String currentUsername() {
        return UserContext.getUsername();
    }

    private String trimToNull(String v) { return StringUtils.hasText(v) ? v.trim() : null; }
    private String trimToEmpty(String v) { return StringUtils.hasText(v) ? v.trim() : ""; }
    private String normalizeDateTime(String value) { if (!StringUtils.hasText(value)) return null; String t = value.trim(); return t.length() == 10 ? t + " 00:00:00" : t; }

    private EquipmentPart findEquipmentPartByCode(String partCode, Long userId) {
        if (!StringUtils.hasText(partCode) || userId == null) return null;
        List<EquipmentPart> matchedParts = equipmentPartMapper.list(new EquipmentPart().setPartCode(partCode.trim()).setCreatorId(userId));
        return matchedParts.isEmpty() ? null : matchedParts.get(0);
    }

    private String resolveInspectionPartName(String partName, EquipmentPart matchedPart) {
        String normalizedName = trimToNull(partName);
        String actualName = matchedPart == null ? null : trimToNull(matchedPart.getPartName());
        return StringUtils.hasText(normalizedName) ? normalizedName : actualName;
    }

    private void fillIdLocationFields(InspectionStandard inspectionStandard) {
        String normalizedCode = trimToNull(inspectionStandard.getIdLocationCode());
        String normalizedName = trimToNull(inspectionStandard.getIdLocationName());
        if (!StringUtils.hasText(normalizedCode) && !StringUtils.hasText(normalizedName)) {
            inspectionStandard.setIdLocationCode(null);
            inspectionStandard.setIdLocationName(null);
            return;
        }
        IDAddress matched = null;
        if (StringUtils.hasText(normalizedCode) && StringUtils.hasText(currentUsername())) {
            matched = idAddressMapper.getByCodeAndCreatorUsername(normalizedCode, currentUsername());
        }
        if (matched == null && StringUtils.hasText(normalizedName) && StringUtils.hasText(currentUsername())) {
            List<IDAddress> byName = idAddressMapper.page(null, normalizedName, null, null, null, currentUsername());
            matched = byName.stream()
                    .filter(item -> item != null && StringUtils.hasText(item.getIdLocationName()))
                    .filter(item -> normalizedName.equals(item.getIdLocationName().trim()))
                    .findFirst().orElse(null);
        }
        if (matched != null) {
            inspectionStandard.setIdLocationCode(trimToNull(matched.getIdLocationCode()));
            inspectionStandard.setIdLocationName(trimToNull(matched.getIdLocationName()));
            return;
        }
        inspectionStandard.setIdLocationCode(normalizedCode);
        inspectionStandard.setIdLocationName(normalizedName);
    }

    private List<String> normalizePartCodes(List<String> partCodes) {
        if (partCodes == null || partCodes.isEmpty()) return null;
        List<String> normalized = new ArrayList<>();
        for (String partCode : partCodes) {
            if (StringUtils.hasText(partCode)) normalized.add(partCode.trim());
        }
        return normalized.isEmpty() ? null : normalized;
    }
}
