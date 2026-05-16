package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.*;
import com.hebei.systemdemo.mapper.*;
import com.hebei.systemdemo.service.IInspectionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class InspectionRecordService implements IInspectionRecordService {
    private static final String RESULT_ABNORMAL = "YC";
    private static final String FLAG_PENDING = "N";
    private static final String FLAG_COMPLETED = "Y";

    @Autowired private InspectionRecordMapper inspectionRecordMapper;
    @Autowired private EquipmentPartMapper equipmentPartMapper;
    @Autowired private EquipmentMapper equipmentMapper;
    @Autowired private InspectionRouteMapper inspectionRouteMapper;
    @Autowired private AbnormalityMapper abnormalityMapper;
    @Autowired private AbnormalityDealMapper abnormalityDealMapper;
    @Autowired private SysUserMapper sysUserMapper;

    @Override
    public Result page(Integer current, Integer size, Long productionLineId, Long routeId, String planDateStart,
                       String planDateEnd, String completionFlag, String partCode, String partName) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<InspectionRecord> page = PageHelper.startPage(current, size);
        List<InspectionRecord> records = inspectionRecordMapper.page(productionLineId, routeId, trimToNull(planDateStart), trimToNull(planDateEnd), trimToNull(completionFlag), trimToNull(partCode), trimToNull(partName), currentUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records); data.put("total", page.getTotal()); data.put("current", current); data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result planSummaryPage(Integer current, Integer size, Long productionLineId, String routeName, String planSource) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<InspectionRecord> page = PageHelper.startPage(current, size);
        List<InspectionRecord> records = inspectionRecordMapper.planSummaryPage(productionLineId, trimToNull(routeName), trimToNull(planSource), currentUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records); data.put("total", page.getTotal()); data.put("current", current); data.put("size", size);
        return Result.ok(data);
    }

    @Override public Result getById(Long id) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (id == null) return Result.fail(ResultCode.BAD_REQUEST, "点检实绩ID不能为空");
        InspectionRecord exist = inspectionRecordMapper.getById(id, currentUserId());
        return exist == null ? Result.fail(ResultCode.NOT_FOUND, "点检实绩不存在或无权限访问") : Result.ok(exist);
    }

    @Override public Result add(InspectionRecord r) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        try { normalize(r); validateAndFill(r, false); } catch (IllegalArgumentException e) { return Result.fail(e.getMessage()); }
        r.setCreatorId(currentUserId()).setCreatedAt(nowString()).setUpdatedAt(nowString());
        int rows = inspectionRecordMapper.add(r);
        return rows <= 0 || r.getId() == null ? Result.fail("新增点检实绩失败") : Result.ok(Collections.singletonMap("id", r.getId()));
    }

    @Override public Result update(InspectionRecord r) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (r.getId() == null) return Result.fail(ResultCode.BAD_REQUEST, "点检实绩ID不能为空");
        if (inspectionRecordMapper.getById(r.getId(), currentUserId()) == null) return Result.fail(ResultCode.NOT_FOUND, "点检实绩不存在或无权限修改");
        try { normalize(r); validateAndFill(r, true); } catch (IllegalArgumentException e) { return Result.fail(e.getMessage()); }
        r.setUpdatedAt(nowString());
        return inspectionRecordMapper.updateById(r, currentUserId()) <= 0 ? Result.fail("修改点检实绩失败") : Result.ok("修改成功", null);
    }

    @Override public Result deleteById(Long id) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (inspectionRecordMapper.getById(id, currentUserId()) == null) return Result.fail(ResultCode.NOT_FOUND, "点检实绩不存在或无权限删除");
        int rows = inspectionRecordMapper.deleteById(id, currentUserId());
        return rows <= 0 ? Result.fail("删除点检实绩失败") : Result.ok("删除成功", Collections.singletonMap("deletedCount", rows));
    }

    @Override public Result batchDelete(List<Long> ids) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");
        for (Long id : ids) if (inspectionRecordMapper.getById(id, currentUserId()) == null) return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的点检实绩");
        int rows = inspectionRecordMapper.batchDeleteByIds(ids, currentUserId());
        return rows <= 0 ? Result.fail("批量删除点检实绩失败") : Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    @Override public Result complete(List<Long> ids) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail(ResultCode.BAD_REQUEST, "完工记录不能为空");
        int completed = 0, abnormalCreated = 0;
        for (Long id : ids) {
            InspectionRecord r = inspectionRecordMapper.getById(id, currentUserId());
            if (r == null) return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的点检实绩");
            if (!StringUtils.hasText(r.getCurrentResult()) || "-".equals(r.getCurrentResult())) return Result.fail("存在未填写当前结果的点检实绩，无法完工");
            if (RESULT_ABNORMAL.equals(normalizeResult(r.getCurrentResult()))) {
                if (!StringUtils.hasText(r.getAbnormalType())) return Result.fail("存在异常但未填写异常类型的点检实绩，无法完工");
                if (!StringUtils.hasText(r.getAbnormalPhenomenon())) return Result.fail("存在异常但未填写异常现象的点检实绩，无法完工");
            }
            if (inspectionRecordMapper.updateById(new InspectionRecord().setId(id).setCompletionFlag(FLAG_COMPLETED).setCompletedAt(nowString()).setUpdatedAt(nowString()), currentUserId()) > 0) {
                completed++;
                if (RESULT_ABNORMAL.equals(normalizeResult(r.getCurrentResult()))) { createAbnormality(r); abnormalCreated++; }
            }
        }
        String msg = abnormalCreated > 0 ? String.format("成功完工%d条记录，并自动创建%d条异常记录，请提醒用户后续到异常录入中完善内容", completed, abnormalCreated) : String.format("成功完工%d条记录", completed);
        return Result.ok(msg, Collections.singletonMap("completedCount", completed));
    }

    @Override public void createPendingRecordFromStandard(InspectionRecord r) {
        if (r == null) return;
        r.setCurrentResult("-").setCompletionFlag(FLAG_PENDING);
        if (!StringUtils.hasText(r.getPlanSource())) r.setPlanSource("DJ");
        if (!StringUtils.hasText(r.getStandardCode())) r.setStandardCode(buildStandardCode(r.getPartCode(), r.getInspectionStandardId()));
        r.setCreatedAt(nowString()).setUpdatedAt(nowString());
        inspectionRecordMapper.add(r);
    }

    private void validateAndFill(InspectionRecord r, boolean isEdit) {
        if (!isEdit && r.getProductionLineId() == null) throw new IllegalArgumentException("生产线不能为空");
        if (!isEdit && !StringUtils.hasText(r.getPartCode())) throw new IllegalArgumentException("设备部位编码不能为空");
        if (!isEdit && !StringUtils.hasText(r.getPlanDate())) throw new IllegalArgumentException("计划日期不能为空");
        String partCode = trimToNull(r.getPartCode());
        if (StringUtils.hasText(partCode)) {
            List<EquipmentPart> parts = equipmentPartMapper.list(new EquipmentPart().setPartCode(partCode).setCreatorId(currentUserId()));
            if (parts.isEmpty()) throw new IllegalArgumentException("设备部位编码不存在或无权限使用");
            EquipmentPart part = parts.get(0);
            if (!StringUtils.hasText(r.getPartName())) r.setPartName(trimToNull(part.getPartName()));
            if (!isEdit && part.getEquipmentId() != null && equipmentMapper.getById(part.getEquipmentId(), currentUserId()) == null) throw new IllegalArgumentException("设备部位未关联有效设备，无法确定所属生产线");
        }
        if (r.getRouteId() != null) {
            InspectionRoute route = inspectionRouteMapper.getById(r.getRouteId(), currentUserId());
            if (route == null) throw new IllegalArgumentException("点检路线不存在或无权限使用");
            if (r.getProductionLineId() != null && route.getProductionLineId() != null && !r.getProductionLineId().equals(route.getProductionLineId())) throw new IllegalArgumentException("所选点检路线不属于当前生产线");
            if (!StringUtils.hasText(r.getRouteCode())) r.setRouteCode(trimToNull(route.getRouteCode()));
            if (!StringUtils.hasText(r.getRouteName())) r.setRouteName(trimToNull(route.getRouteName()));
        }
        if (!StringUtils.hasText(r.getPlanSource())) r.setPlanSource("DJ");
        if (!StringUtils.hasText(r.getCompletionFlag())) r.setCompletionFlag(FLAG_PENDING);
        if (!StringUtils.hasText(r.getCurrentResult())) r.setCurrentResult("-");
        if (!StringUtils.hasText(r.getStandardCode()) && StringUtils.hasText(r.getPartCode())) r.setStandardCode(buildStandardCode(r.getPartCode(), r.getInspectionStandardId()));
    }

    private void createAbnormality(InspectionRecord r) {
        String now = nowString();
        Abnormality abnormality = new Abnormality()
                .setAbnormalCode("ABN-IR-" + (r.getId() == null ? System.currentTimeMillis() : r.getId()))
                .setDeviceUnitCode(trimToNull(r.getPartCode()))
                .setSource("点检实绩自动生成")
                .setAbnormalLocation(trimToNull(r.getInspectionPart()))
                .setAbnormalType(trimToNull(r.getAbnormalType()))
                .setSafetyOutput("02")
                .setReporter(currentRealName())
                .setAbnormalDescription(trimToNull(r.getAbnormalPhenomenon()))
                .setReportDate(LocalDate.now().toString())
                .setStatus("00")
                .setCreatorUsername(currentUsername())
                .setCreatorName(currentRealName())
                .setCreatedAt(now)
                .setUpdatedAt(now);
        int rows = abnormalityMapper.add(abnormality);
        if (rows > 0 && abnormality.getId() != null) {
            abnormalityDealMapper.add(new AbnormalityDeal().setAbnormalityId(abnormality.getId()).setCreatedAt(now).setUpdatedAt(now));
        }
    }

    private void normalize(InspectionRecord r) {
        r.setRouteCode(trimToNull(r.getRouteCode())).setRouteName(trimToNull(r.getRouteName())).setPlanDate(trimToNull(r.getPlanDate())).setCurrentResult(normalizeResult(r.getCurrentResult())).setAbnormalType(trimToNull(r.getAbnormalType())).setAbnormalPhenomenon(trimToNull(r.getAbnormalPhenomenon())).setPlanSource(trimToNull(r.getPlanSource())).setStandardCode(trimToNull(r.getStandardCode())).setPartCode(trimToNull(r.getPartCode())).setPartName(trimToNull(r.getPartName())).setInspectionPart(trimToNull(r.getInspectionPart())).setInspectionContent(trimToNull(r.getInspectionContent())).setCompletedAt(trimToNull(r.getCompletedAt())).setCycleUnit(trimToNull(r.getCycleUnit())).setCompletionFlag(trimToNull(r.getCompletionFlag())).setQualitativeStandard(trimToNull(r.getQualitativeStandard())).setStandardCategory(trimToNull(r.getStandardCategory())).setQuantitativeStandard(trimToNull(r.getQuantitativeStandard())).setUnitOfMeasurement(trimToNull(r.getUnitOfMeasurement())).setUpperLimit(trimToNull(r.getUpperLimit())).setLowerLimit(trimToNull(r.getLowerLimit())).setIdLocationCode(trimToNull(r.getIdLocationCode())).setIdLocationName(trimToNull(r.getIdLocationName())).setAbnormalContactSheet(trimToNull(r.getAbnormalContactSheet()));
    }

    private String normalizeResult(String v) { String t = trimToNull(v); if (!StringUtils.hasText(t)) return null; if ("ZC-正常".equals(t)) return "ZC"; if ("YC-异常".equals(t)) return RESULT_ABNORMAL; return t; }
    private String buildStandardCode(String partCode, Long standardId) { return partCode + "-DJ-" + (standardId == null ? "0" : standardId); }
    private Long currentUserId() { SysUser user = UserContext.getUser(); return user == null ? null : user.getId(); }
    private String currentUsername() { SysUser user = UserContext.getUser(); return user == null ? null : trimToNull(user.getUsername()); }
    private String currentRealName() { Long userId = UserContext.getUserId(); if (userId == null) return null; SysUser user = sysUserMapper.getById(userId); return user == null ? null : trimToNull(user.getRealName()); }
    private String trimToNull(String v) { if (!StringUtils.hasText(v)) return null; return v.trim(); }
    private String nowString() { return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); }
}
