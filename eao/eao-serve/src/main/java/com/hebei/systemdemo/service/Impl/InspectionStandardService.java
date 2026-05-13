package com.hebei.systemdemo.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.domain.dto.inspectionStandard.InspectionStandardImportDTO;
import com.hebei.systemdemo.domain.po.*;
import com.hebei.systemdemo.mapper.*;
import com.hebei.systemdemo.service.IInspectionRecordService;
import com.hebei.systemdemo.service.IInspectionStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InspectionStandardService implements IInspectionStandardService {
    @Autowired private InspectionStandardMapper inspectionStandardMapper;
    @Autowired private EquipmentPartMapper equipmentPartMapper;
    @Autowired private EquipmentMapper equipmentMapper;
    @Autowired private DeviceUnitMapper deviceUnitMapper;
    @Autowired private IDAddressMapper idAddressMapper;
    @Autowired private IInspectionRecordService inspectionRecordService;

    @Override
    public Result page(Integer current, Integer size, String inspectionName, String partCode, List<String> partCodes,
                       String partName, String cycleUnit, String dataType, String signalType,
                       String implementationMethod, String standardCategory, String unitOfMeasurement,
                       String profession) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<InspectionStandard> page = PageHelper.startPage(current, size);
        List<InspectionStandard> records = inspectionStandardMapper.page(trimToNull(inspectionName), trimToNull(partCode), normalizePartCodes(partCodes), trimToNull(partName), trimToNull(cycleUnit), trimToNull(dataType), trimToNull(signalType), trimToNull(implementationMethod), trimToNull(standardCategory), trimToNull(unitOfMeasurement), trimToNull(profession), currentUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records); data.put("total", page.getTotal()); data.put("current", current); data.put("size", size);
        return Result.ok(data);
    }

    @Override public Result getById(Long id) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        InspectionStandard s = inspectionStandardMapper.getById(id, currentUserId());
        return s == null ? Result.fail("未找到点检标准信息或无权限访问") : Result.ok(s);
    }

    @Override public Result add(InspectionStandard s) {
        if (!StringUtils.hasText(s.getPartCode())) return Result.fail("设备部位编码不能为空");
        EquipmentPart part = findEquipmentPartByCode(s.getPartCode().trim(), currentUserId());
        if (part == null) return Result.fail("设备部位编码[" + s.getPartCode().trim() + "]不存在或无权限使用，新增失败");
        s.setPartCode(trimToNull(part.getPartCode())).setPartName(resolveInspectionPartName(s.getPartName(), part));
        fillIdLocationFields(s);
        s.setCreatorId(currentUserId()).setCreatedAt(normalizeDateTime(s.getCreatedAt())).setUpdatedAt(normalizeDateTime(s.getUpdatedAt()));
        int rows = inspectionStandardMapper.add(s);
        if (rows <= 0 || s.getId() == null) return Result.fail("新增点检标准失败");
        createInspectionRecordFromStandard(s, part, currentUserId());
        return Result.ok(Collections.singletonMap("id", s.getId()));
    }

    @Override public Result batchAdd(InspectionStandardImportDTO importDTO, Long userId) {
        if (importDTO == null || importDTO.getInspectionStandardList() == null || importDTO.getInspectionStandardList().isEmpty()) return Result.fail("导入数据不能为空");
        List<String> failedRecords = new ArrayList<>(); int successCount = 0;
        for (var dto : importDTO.getInspectionStandardList()) {
            try {
                InspectionStandard s = BeanUtil.copyProperties(dto, InspectionStandard.class);
                String name = trimToEmpty(s.getInspectionName()), partCode = trimToEmpty(s.getPartCode());
                if (!StringUtils.hasText(name)) { failedRecords.add("点检标准名称[为空]：点检标准名称不能为空"); continue; }
                if (!StringUtils.hasText(partCode)) { failedRecords.add("点检标准[" + name + "]：设备部位编码不能为空"); continue; }
                EquipmentPart part = findEquipmentPartByCode(partCode, userId);
                if (part == null) { failedRecords.add("点检标准[" + name + "]：设备部位编码[" + partCode + "]不存在或无权限，已跳过"); continue; }
                s.setInspectionName(name).setPartCode(trimToNull(part.getPartCode())).setPartName(resolveInspectionPartName(s.getPartName(), part));
                fillIdLocationFields(s);
                s.setCreatorId(userId).setCreatedAt(normalizeDateTime(s.getCreatedAt())).setUpdatedAt(normalizeDateTime(s.getUpdatedAt()));
                int rows = inspectionStandardMapper.add(s);
                if (rows > 0 && s.getId() != null) { successCount++; createInspectionRecordFromStandard(s, part, userId); }
                else failedRecords.add("点检标准[" + name + "]：新增失败");
            } catch (Exception e) {
                failedRecords.add("点检标准[" + dto.getInspectionName() + "]：异常 - " + e.getMessage());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount); result.put("failedCount", failedRecords.size()); result.put("failedRecords", failedRecords);
        return failedRecords.isEmpty() ? Result.ok("批量新增成功，共" + successCount + "条", result) : Result.ok("部分成功，成功" + successCount + "条，失败" + failedRecords.size() + "条", result);
    }

    @Override public Result update(InspectionStandard s) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (s.getId() == null) return Result.fail("点检标准ID不能为空");
        InspectionStandard exist = inspectionStandardMapper.getById(s.getId(), currentUserId());
        if (exist == null) return Result.fail("点检标准不存在或无权限修改");
        if (StringUtils.hasText(s.getPartCode())) {
            EquipmentPart part = findEquipmentPartByCode(s.getPartCode().trim(), currentUserId());
            if (part == null) return Result.fail("设备部位编码不存在或无权限使用");
            s.setPartCode(trimToNull(part.getPartCode())).setPartName(resolveInspectionPartName(s.getPartName(), part));
        }
        fillIdLocationFields(s); s.setUpdatedAt(normalizeDateTime(s.getUpdatedAt()));
        return inspectionStandardMapper.updateById(s) <= 0 ? Result.fail("更新点检标准失败") : Result.ok();
    }

    @Override public Result deleteById(Long id) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        InspectionStandard exist = inspectionStandardMapper.getById(id, currentUserId());
        if (exist == null) return Result.fail("点检标准不存在或无权限删除");
        return inspectionStandardMapper.deleteById(id) <= 0 ? Result.fail("删除点检标准失败") : Result.ok();
    }

    @Override public Result getStandardListByPartCode(String partCode) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        EquipmentPart matchedPart = findEquipmentPartByCode(partCode, currentUserId());
        if (matchedPart == null) return Result.ok(Collections.emptyList());
        List<Map<String, Object>> list = inspectionStandardMapper.list(new InspectionStandard().setPartCode(trimToNull(partCode)).setCreatorId(currentUserId())).stream().map(s -> {
            Map<String, Object> result = new HashMap<>(); result.put("id", s.getId()); result.put("inspectionName", s.getInspectionName()); return result; }).collect(Collectors.toList());
        return Result.ok(list);
    }

    private void createInspectionRecordFromStandard(InspectionStandard s, EquipmentPart part, Long userId) {
        if (s == null || part == null || userId == null || part.getEquipmentId() == null) return;
        Equipment equipment = equipmentMapper.getById(part.getEquipmentId(), userId);
        if (equipment == null || equipment.getDeviceUnitId() == null) return;
        DeviceUnit unit = deviceUnitMapper.getById(equipment.getDeviceUnitId(), userId);
        if (unit == null || unit.getProductionLineId() == null) return;
        InspectionRecord r = new InspectionRecord();
        r.setInspectionStandardId(s.getId());
        r.setProductionLineId(unit.getProductionLineId());
        r.setPlanDate(LocalDate.now().toString());
        r.setPlanSource("DJ");
        r.setStandardCode(trimToNull(part.getPartCode()) + "-DJ-" + s.getId());
        r.setPartCode(trimToNull(part.getPartCode()));
        r.setPartName(resolveInspectionPartName(s.getPartName(), part));
        r.setInspectionPart(trimToNull(s.getInspectionPart()));
        r.setInspectionContent(trimToNull(s.getInspectionContent()));
        r.setImplementationCycle(s.getImplementationCycle());
        r.setCycleUnit(trimToNull(s.getCycleUnit()));
        r.setCompletionFlag("N");
        r.setCurrentResult("-");
        r.setQualitativeStandard(trimToNull(s.getQualitativeStandard()));
        r.setStandardCategory(trimToNull(s.getStandardCategory()));
        r.setQuantitativeStandard(trimToNull(s.getQuantitativeStandard()));
        r.setUnitOfMeasurement(trimToNull(s.getUnitOfMeasurement()));
        r.setUpperLimit(trimToNull(s.getUpperLimit()));
        r.setLowerLimit(trimToNull(s.getLowerLimit()));
        r.setIdLocationCode(trimToNull(s.getIdLocationCode()));
        r.setIdLocationName(trimToNull(s.getIdLocationName()));
        r.setCreatorId(userId);
        inspectionRecordService.createPendingRecordFromStandard(r);
    }

    private Long currentUserId() { SysUser user = UserContext.getUser(); return user == null ? null : user.getId(); }
    private String currentUsername() { return UserContext.getUsername(); }
    private String trimToNull(String v) { return StringUtils.hasText(v) ? v.trim() : null; }
    private String trimToEmpty(String v) { return StringUtils.hasText(v) ? v.trim() : ""; }
    private String normalizeDateTime(String v) { if (!StringUtils.hasText(v)) return null; String t = v.trim(); return t.length() == 10 ? t + " 00:00:00" : t; }
    private EquipmentPart findEquipmentPartByCode(String partCode, Long userId) { if (!StringUtils.hasText(partCode) || userId == null) return null; List<EquipmentPart> parts = equipmentPartMapper.list(new EquipmentPart().setPartCode(partCode.trim()).setCreatorId(userId)); return parts.isEmpty() ? null : parts.get(0); }
    private String resolveInspectionPartName(String partName, EquipmentPart part) { String normalized = trimToNull(partName); String actual = part == null ? null : trimToNull(part.getPartName()); return StringUtils.hasText(normalized) ? normalized : actual; }

    private void fillIdLocationFields(InspectionStandard s) {
        String code = trimToNull(s.getIdLocationCode()), name = trimToNull(s.getIdLocationName());
        if (!StringUtils.hasText(code) && !StringUtils.hasText(name)) { s.setIdLocationCode(null); s.setIdLocationName(null); return; }
        IDAddress matched = null;
        if (StringUtils.hasText(code) && StringUtils.hasText(currentUsername())) matched = idAddressMapper.getByCodeAndCreatorUsername(code, currentUsername());
        if (matched == null && StringUtils.hasText(name) && StringUtils.hasText(currentUsername())) {
            List<IDAddress> byName = idAddressMapper.page(null, name, null, null, null, currentUsername());
            matched = byName.stream().filter(x -> x != null && StringUtils.hasText(x.getIdLocationName())).filter(x -> name.equals(x.getIdLocationName().trim())).findFirst().orElse(null);
        }
        if (matched != null) { s.setIdLocationCode(trimToNull(matched.getIdLocationCode())); s.setIdLocationName(trimToNull(matched.getIdLocationName())); }
        else { s.setIdLocationCode(code); s.setIdLocationName(name); }
    }

    private List<String> normalizePartCodes(List<String> partCodes) {
        if (partCodes == null || partCodes.isEmpty()) return null;
        List<String> normalized = new ArrayList<>();
        for (String partCode : partCodes) if (StringUtils.hasText(partCode)) normalized.add(partCode.trim());
        return normalized.isEmpty() ? null : normalized;
    }
}
