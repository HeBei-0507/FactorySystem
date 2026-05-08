package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.domain.dto.maintenanceStandard.MaintenanceStandardAddDTO;
import com.hebei.systemdemo.domain.po.EquipmentPart;
import com.hebei.systemdemo.domain.po.InboundRequest;
import com.hebei.systemdemo.domain.po.MaintenanceStandard;
import com.hebei.systemdemo.domain.po.MaintenanceStandardSafetyTag;
import com.hebei.systemdemo.domain.po.MaintenanceStandardSparePart;
import com.hebei.systemdemo.domain.po.MaintenanceStandardTool;
import com.hebei.systemdemo.domain.po.MaterialCode;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.EquipmentPartMapper;
import com.hebei.systemdemo.mapper.InboundRequestMapper;
import com.hebei.systemdemo.mapper.MaintenanceStandardMapper;
import com.hebei.systemdemo.mapper.MaintenanceStandardSafetyTagMapper;
import com.hebei.systemdemo.mapper.MaintenanceStandardSparePartMapper;
import com.hebei.systemdemo.mapper.MaintenanceStandardToolMapper;
import com.hebei.systemdemo.mapper.MaterialCodeMapper;
import com.hebei.systemdemo.service.IMaintenanceStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MaintenanceStandardService implements IMaintenanceStandardService {
    @Autowired private MaintenanceStandardMapper maintenanceStandardMapper;
    @Autowired private MaintenanceStandardSparePartMapper sparePartMapper;
    @Autowired private MaintenanceStandardToolMapper toolMapper;
    @Autowired private MaintenanceStandardSafetyTagMapper safetyTagMapper;
    @Autowired private EquipmentPartMapper equipmentPartMapper;
    @Autowired private MaterialCodeMapper materialCodeMapper;
    @Autowired private InboundRequestMapper inboundRequestMapper;

    @Override
    public Result page(Integer current, Integer size, String standardCode, String partCode, String partName,
                       String itemName, String profession, String maintenanceCategory,
                       String maintenanceStartTime, String maintenanceEndTime, String workCategory,
                       String workContent, List<String> partCodes) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<MaintenanceStandard> page = PageHelper.startPage(current, size);
        List<MaintenanceStandard> records = maintenanceStandardMapper.page(
                t(standardCode), t(partCode), t(partName), t(itemName), t(profession), t(maintenanceCategory),
                t(maintenanceStartTime), t(maintenanceEndTime), t(workCategory), t(workContent), normCodes(partCodes), currentUserId());
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
        if (id == null) return Result.fail("检修标准ID不能为空");
        MaintenanceStandard standard = maintenanceStandardMapper.getById(id, currentUserId());
        if (standard == null) return Result.fail("检修标准不存在或无权限访问");
        Map<String, Object> data = new HashMap<>();
        data.put("standard", standard);
        data.put("spareParts", sparePartMapper.listByStandardId(id));
        data.put("tools", toolMapper.listByStandardId(id));
        data.put("safetyTags", safetyTagMapper.listByStandardId(id));
        return Result.ok(data);
    }

    @Override
    @Transactional
    public Result add(MaintenanceStandard ms, MaintenanceStandardAddDTO dto) {
        if (!fillBase(ms)) return Result.fail("设备部位不存在");
        fillUser(ms);
        ms.setStandardCode(genCode());
        ms.setCreatedAt(now());
        ms.setUpdatedAt(now());
        int rows = maintenanceStandardMapper.add(ms);
        if (rows <= 0 || ms.getId() == null) return Result.fail("新增检修标准失败");
        saveDetails(ms.getId(), dto);
        return Result.ok(Map.of("id", ms.getId(), "standardCode", ms.getStandardCode()));
    }

    @Override
    @Transactional
    public Result update(MaintenanceStandard ms, MaintenanceStandardAddDTO dto) {
        if (ms.getId() == null) return Result.fail("检修标准ID不能为空");
        MaintenanceStandard exist = maintenanceStandardMapper.getById(ms.getId(), currentUserId());
        if (exist == null) return Result.fail("检修标准不存在或无权限修改");
        if (!fillBase(ms)) return Result.fail("设备部位不存在");
        ms.setStandardCode(exist.getStandardCode());
        ms.setMaintainerId(exist.getMaintainerId());
        ms.setMaintainerName(exist.getMaintainerName());
        ms.setMaintainerUsername(exist.getMaintainerUsername());
        ms.setUpdatedAt(now());
        if (maintenanceStandardMapper.updateById(ms) <= 0) return Result.fail("修改检修标准失败");
        sparePartMapper.deleteByStandardId(ms.getId());
        toolMapper.deleteByStandardId(ms.getId());
        safetyTagMapper.deleteByStandardId(ms.getId());
        saveDetails(ms.getId(), dto);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result deleteById(Long id) {
        if (id == null) return Result.fail("检修标准ID不能为空");
        MaintenanceStandard exist = maintenanceStandardMapper.getById(id, currentUserId());
        if (exist == null) return Result.fail("检修标准不存在或无权限删除");
        sparePartMapper.deleteByStandardId(id);
        toolMapper.deleteByStandardId(id);
        safetyTagMapper.deleteByStandardId(id);
        if (maintenanceStandardMapper.deleteById(id, currentUserId()) <= 0) return Result.fail("删除检修标准失败");
        return Result.ok();
    }

    @Override
    public Result getMaterialCandidates(Integer current, Integer size, String materialCode, String materialName,
                                        String materialSubCategory, String inboundStatus) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        PageHelper.startPage(current, size);
        List<MaterialCode> materials = materialCodeMapper.page(t(materialCode), t(materialName), null, t(materialSubCategory), null, "02-激活", currentUserId());
        List<Map<String, Object>> records = new ArrayList<>();
        for (MaterialCode m : materials) {
            List<InboundRequest> ins = inboundRequestMapper.page(null, null, m.getMaterialCode(), null, null, null, t(inboundStatus) == null ? "10-已确认入库" : t(inboundStatus), currentUserId());
            for (InboundRequest in : ins) {
                Map<String, Object> x = new HashMap<>();
                x.put("materialCodeId", m.getId());
                x.put("inboundRequestId", in.getId());
                x.put("materialCode", m.getMaterialCode());
                x.put("materialName", m.getMaterialName());
                x.put("materialSubCategory", m.getMaterialSubCategory());
                x.put("modelSpecification", m.getModelSpecification());
                x.put("quantityUnit", unitOf(m.getMaterialSubCategory()));
                x.put("inboundNo", in.getInboundNo());
                x.put("inboundStatus", in.getInboundStatus());
                records.add(x);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", records.size());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    private boolean fillBase(MaintenanceStandard ms) {
        String partCode = t(ms.getPartCode());
        if (!StringUtils.hasText(partCode)) return false;
        List<EquipmentPart> parts = equipmentPartMapper.list(new EquipmentPart().setPartCode(partCode));
        if (parts == null || parts.isEmpty()) return false;
        EquipmentPart part = parts.get(0);
        ms.setPartCode(partCode);
        ms.setPartName(StringUtils.hasText(ms.getPartName()) ? t(ms.getPartName()) : t(part.getPartName()));
        ms.setItemName(t(ms.getItemName()));
        ms.setRiskFactor(t(ms.getRiskFactor()));
        ms.setSafetyMeasure(t(ms.getSafetyMeasure()));
        ms.setWorkCategory(t(ms.getWorkCategory()));
        ms.setWorkContent(t(ms.getWorkContent()));
        ms.setAcceptanceLevel(t(ms.getAcceptanceLevel()));
        ms.setMaintenanceCategory(t(ms.getMaintenanceCategory()));
        ms.setProfession(t(ms.getProfession()));
        ms.setCycleUnit(t(ms.getCycleUnit()));
        ms.setLastCompletionDate(t(ms.getLastCompletionDate()));
        ms.setNextScheduleDate(t(ms.getNextScheduleDate()));
        ms.setMaintenanceStartTime(t(ms.getMaintenanceStartTime()));
        ms.setMaintenanceEndTime(t(ms.getMaintenanceEndTime()));
        return true;
    }

    private void fillUser(MaintenanceStandard ms) {
        SysUser user = UserContext.getUser();
        ms.setMaintainerId(user == null ? null : user.getId());
        ms.setMaintainerUsername(user == null ? null : t(user.getUsername()));
        ms.setMaintainerName(user == null ? null : t(user.getRealName()));
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private void saveDetails(Long standardId, MaintenanceStandardAddDTO dto) {
        if (dto == null || standardId == null) return;
        if (dto.getSpareParts() != null) {
            dto.getSpareParts().forEach(item -> {
                if (item.getMaterialCodeId() == null || item.getInboundRequestId() == null) return;
                sparePartMapper.add(new MaintenanceStandardSparePart().setStandardId(standardId).setMaterialCodeId(item.getMaterialCodeId()).setInboundRequestId(item.getInboundRequestId()).setMaterialCode(t(item.getMaterialCode())).setMaterialName(t(item.getMaterialName())).setMaterialSubCategory(t(item.getMaterialSubCategory())).setModelSpecification(t(item.getModelSpecification())).setQuantity(item.getQuantity()).setQuantityUnit(t(item.getQuantityUnit())).setCreatedAt(now()).setUpdatedAt(now()));
            });
        }
        if (dto.getTools() != null) {
            dto.getTools().forEach(item -> {
                if (item.getMaterialCodeId() == null || item.getInboundRequestId() == null) return;
                toolMapper.add(new MaintenanceStandardTool().setStandardId(standardId).setMaterialCodeId(item.getMaterialCodeId()).setInboundRequestId(item.getInboundRequestId()).setMaterialCode(t(item.getMaterialCode())).setMaterialName(t(item.getMaterialName())).setMaterialSubCategory(t(item.getMaterialSubCategory())).setModelSpecification(t(item.getModelSpecification())).setQuantity(item.getQuantity()).setQuantityUnit(t(item.getQuantityUnit())).setCreatedAt(now()).setUpdatedAt(now()));
            });
        }
        if (dto.getSafetyTags() != null) {
            dto.getSafetyTags().forEach(item -> {
                if (!StringUtils.hasText(item.getTagNature()) && !StringUtils.hasText(item.getTagLocation()) && !StringUtils.hasText(item.getTagDeviceCode())) return;
                safetyTagMapper.add(new MaintenanceStandardSafetyTag().setStandardId(standardId).setTagNature(t(item.getTagNature())).setTagLocation(t(item.getTagLocation())).setTagDeviceCode(t(item.getTagDeviceCode())).setCreatedAt(now()).setUpdatedAt(now()));
            });
        }
    }

    private List<String> normCodes(List<String> partCodes) {
        if (partCodes == null) return null;
        List<String> list = partCodes.stream().map(this::t).filter(StringUtils::hasText).distinct().collect(Collectors.toList());
        return list.isEmpty() ? null : list;
    }

    private String genCode() { return "MS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")); }
    private String unitOf(String subCategory) { String x = t(subCategory); return !StringUtils.hasText(x) ? "个" : (x.contains("工器具") ? "套" : "个"); }
    private String t(String value) { return !StringUtils.hasText(value) ? null : value.trim(); }
    private String now() { return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
}
