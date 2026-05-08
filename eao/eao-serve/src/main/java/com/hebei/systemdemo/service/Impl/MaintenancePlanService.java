package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.domain.po.MaintenancePlan;
import com.hebei.systemdemo.domain.po.ProductionLine;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.MaintenancePlanMapper;
import com.hebei.systemdemo.mapper.ProductionLineMapper;
import com.hebei.systemdemo.service.IMaintenancePlanService;
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
public class MaintenancePlanService implements IMaintenancePlanService {
    private static final String STATUS_PENDING = "00-待送审";
    private static final String STATUS_SUBMITTED = "10-已送审";
    private static final String STATUS_APPROVED = "20-已通过";

    @Autowired
    private MaintenancePlanMapper maintenancePlanMapper;

    @Autowired
    private ProductionLineMapper productionLineMapper;

    @Override
    public Result page(Integer current, Integer size, String planCode, String productionLineCode,
                       String maintenanceCategory, String status, Boolean excludePending) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<MaintenancePlan> page = PageHelper.startPage(current, size);
        List<MaintenancePlan> records = maintenancePlanMapper.page(
                t(planCode), t(productionLineCode), t(maintenanceCategory), t(status),
                Boolean.TRUE.equals(excludePending) ? STATUS_PENDING : null, currentUserId());
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
        if (id == null) return Result.fail("检修计划ID不能为空");
        MaintenancePlan plan = maintenancePlanMapper.getById(id, currentUserId());
        if (plan == null) return Result.fail("检修计划不存在或无权限访问");
        return Result.ok(plan);
    }

    @Override
    @Transactional
    public Result add(MaintenancePlan maintenancePlan) {
        Result valid = fillAndValidateBase(maintenancePlan, false);
        if (valid != null) return valid;
        fillUser(maintenancePlan);
        maintenancePlan.setPlanCode(genCode());
        maintenancePlan.setStatus(defaultStatus(maintenancePlan.getStatus()));
        maintenancePlan.setCreatedAt(now());
        maintenancePlan.setUpdatedAt(now());
        if (maintenancePlanMapper.add(maintenancePlan) <= 0 || maintenancePlan.getId() == null) {
            return Result.fail("新增检修计划失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", maintenancePlan.getId());
        data.put("planCode", maintenancePlan.getPlanCode());
        return Result.ok(data);
    }

    @Override
    @Transactional
    public Result update(MaintenancePlan maintenancePlan) {
        if (maintenancePlan.getId() == null) return Result.fail("检修计划ID不能为空");
        MaintenancePlan exist = maintenancePlanMapper.getById(maintenancePlan.getId(), currentUserId());
        if (exist == null) return Result.fail("检修计划不存在或无权限修改");
        if (!STATUS_PENDING.equals(exist.getStatus())) return Result.fail("仅待送审状态的计划允许修改");
        Result valid = fillAndValidateBase(maintenancePlan, true);
        if (valid != null) return valid;
        maintenancePlan.setPlanCode(exist.getPlanCode());
        maintenancePlan.setStatus(defaultStatus(maintenancePlan.getStatus()));
        maintenancePlan.setCreatorId(exist.getCreatorId());
        maintenancePlan.setCreatorUsername(exist.getCreatorUsername());
        maintenancePlan.setCreatorName(exist.getCreatorName());
        maintenancePlan.setCreatedAt(exist.getCreatedAt());
        maintenancePlan.setUpdatedAt(now());
        if (maintenancePlanMapper.updateById(maintenancePlan) <= 0) return Result.fail("修改检修计划失败");
        return Result.ok();
    }

    @Override
    @Transactional
    public Result deleteByIds(List<Long> ids) {
        List<Long> validIds = normIds(ids);
        if (validIds.isEmpty()) return Result.fail("请选择要删除的检修计划");
        int rows = maintenancePlanMapper.deleteByIds(validIds, now(), STATUS_PENDING, currentUserId());
        if (rows <= 0) return Result.fail("仅本人待送审状态的计划允许删除");
        return Result.ok();
    }

    @Override
    @Transactional
    public Result submitByIds(List<Long> ids) {
        return changeStatus(ids, STATUS_PENDING, STATUS_SUBMITTED, "请选择待送审的检修计划送审");
    }

    @Override
    @Transactional
    public Result approveByIds(List<Long> ids) {
        return changeStatus(ids, STATUS_SUBMITTED, STATUS_APPROVED, "请选择已送审的检修计划进行同意");
    }

    @Override
    @Transactional
    public Result rollbackApproveByIds(List<Long> ids) {
        return changeStatus(ids, STATUS_SUBMITTED, STATUS_PENDING, "请选择已送审的检修计划进行回退");
    }

    private Result changeStatus(List<Long> ids, String fromStatus, String toStatus, String emptyTip) {
        List<Long> validIds = normIds(ids);
        if (validIds.isEmpty()) return Result.fail(emptyTip);
        int rows = maintenancePlanMapper.updateStatusByIds(validIds, fromStatus, toStatus, now(), currentUserId());
        if (rows <= 0) return Result.fail("状态更新失败，请确认所选计划状态是否正确或是否属于本人");
        return Result.ok();
    }

    private Result fillAndValidateBase(MaintenancePlan maintenancePlan, boolean isUpdate) {
        String productionLineCode = t(maintenancePlan.getProductionLineCode());
        if (!StringUtils.hasText(productionLineCode)) return Result.fail("生产线代码不能为空");
        ProductionLine line = findLineByCode(productionLineCode);
        if (line == null) return Result.fail("生产线不存在");
        maintenancePlan.setProductionLineCode(t(line.getLineCode()));
        maintenancePlan.setProductionLineName(t(line.getLineName()));
        maintenancePlan.setMaintenanceCategory(t(maintenancePlan.getMaintenanceCategory()));
        maintenancePlan.setMaintenanceStartTime(t(maintenancePlan.getMaintenanceStartTime()));
        maintenancePlan.setMaintenanceEndTime(t(maintenancePlan.getMaintenanceEndTime()));
        if (!StringUtils.hasText(maintenancePlan.getMaintenanceCategory())) return Result.fail("检修分类不能为空");
        if (!StringUtils.hasText(maintenancePlan.getMaintenanceStartTime())) return Result.fail("检修开始时间不能为空");
        if (!StringUtils.hasText(maintenancePlan.getMaintenanceEndTime())) return Result.fail("检修结束时间不能为空");
        if (maintenancePlan.getMaintenanceStartTime().compareTo(maintenancePlan.getMaintenanceEndTime()) > 0) {
            return Result.fail("检修开始时间不能晚于检修结束时间");
        }
        if (!isUpdate && !StringUtils.hasText(maintenancePlan.getStatus())) {
            maintenancePlan.setStatus(STATUS_PENDING);
        }
        return null;
    }

    private ProductionLine findLineByCode(String lineCode) {
        List<ProductionLine> list = productionLineMapper.list();
        if (list == null || list.isEmpty()) return null;
        return list.stream().filter(item -> lineCode.equals(t(item.getLineCode()))).findFirst().orElse(null);
    }

    private void fillUser(MaintenancePlan maintenancePlan) {
        SysUser user = UserContext.getUser();
        maintenancePlan.setCreatorId(user == null ? null : user.getId());
        maintenancePlan.setCreatorUsername(user == null ? null : t(user.getUsername()));
        maintenancePlan.setCreatorName(user == null ? null : t(user.getRealName()));
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private String defaultStatus(String status) {
        return StringUtils.hasText(status) ? t(status) : STATUS_PENDING;
    }

    private List<Long> normIds(List<Long> ids) {
        if (ids == null) return new ArrayList<>();
        return ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    private String genCode() {
        return "MP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String t(String value) {
        return !StringUtils.hasText(value) ? null : value.trim();
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
