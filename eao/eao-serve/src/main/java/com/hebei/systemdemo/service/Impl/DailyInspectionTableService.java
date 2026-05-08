package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.DailyInspectionTable;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.DailyInspectionTableMapper;
import com.hebei.systemdemo.service.IDailyInspectionTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyInspectionTableService implements IDailyInspectionTableService {
    @Autowired
    private DailyInspectionTableMapper dailyInspectionTableMapper;

    @Override
    public Result addDailyInspectionTable(DailyInspectionTable dailyInspectionTable) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        dailyInspectionTable.setCreatorId(currentUserId());
        dailyInspectionTable.setCreatedAt(normalizeDateTime(dailyInspectionTable.getCreatedAt()));
        dailyInspectionTable.setUpdatedAt(normalizeDateTime(dailyInspectionTable.getUpdatedAt()));
        dailyInspectionTable.setTableCode(generateTableCode());
        dailyInspectionTable.setImplementer(generateImplementer());

        int rows = dailyInspectionTableMapper.addDailyInspectionTable(dailyInspectionTable);
        if (rows <= 0 || dailyInspectionTable.getId() == null) return Result.fail("新增日常巡检表失败");
        return Result.ok(Collections.singletonMap("id", dailyInspectionTable.getId()));
    }

    @Override
    public Result updateDailyInspectionTable(DailyInspectionTable dailyInspectionTable) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (dailyInspectionTable.getId() == null) return Result.fail(ResultCode.BAD_REQUEST, "ID不能为空");

        DailyInspectionTable existTable = dailyInspectionTableMapper.getById(dailyInspectionTable.getId(), currentUserId());
        if (existTable == null) return Result.fail(ResultCode.NOT_FOUND, "日常巡检表不存在或无权限访问");

        dailyInspectionTable.setUpdatedAt(normalizeDateTime(dailyInspectionTable.getUpdatedAt()));
        int rows = dailyInspectionTableMapper.updateDailyInspectionTable(dailyInspectionTable);
        if (rows <= 0) return Result.fail("更新日常巡检表失败");

        return Result.ok("更新成功", null);
    }

    @Override
    public Result page(Integer current, Integer size, String productionLineName, String tableName, String tableCode, String shiftMode) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");

        String normalizedProductionLineName = trimToNull(productionLineName);
        String normalizedTableName = trimToNull(tableName);
        String normalizedTableCode = trimToNull(tableCode);
        String normalizedShiftMode = trimToNull(shiftMode);

        Page<DailyInspectionTable> page = PageHelper.startPage(current, size);
        List<DailyInspectionTable> records = dailyInspectionTableMapper.page(normalizedProductionLineName, normalizedTableName, normalizedTableCode, normalizedShiftMode, currentUserId());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result batchDeleteDailyInspectionTable(List<Long> ids) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");

        List<Long> deletableIds = new ArrayList<>();
        for (Long id : ids) {
            DailyInspectionTable existTable = dailyInspectionTableMapper.getById(id, currentUserId());
            if (existTable != null) deletableIds.add(id);
        }
        if (deletableIds.isEmpty()) return Result.fail("未找到可删除的日常巡检表或无权限删除");

        int rows = dailyInspectionTableMapper.batchDeleteByIds(deletableIds);
        if (rows <= 0) return Result.fail("删除日常巡检表失败");

        return Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    private Long currentUserId() {
        SysUser user = UserContext.getUser();
        return user == null ? null : user.getId();
    }

    private String generateTableCode() {
        Long creatorId = UserContext.getUserId();
        String username = UserContext.getUsername();
        if (!StringUtils.hasText(username)) throw new RuntimeException("用户信息不存在");

        String maxTableCode = dailyInspectionTableMapper.getMaxTableCodeByCreatorId(creatorId);
        int nextSequence = 1;
        if (StringUtils.hasText(maxTableCode)) {
            String prefix = username + "XJ";
            if (maxTableCode.startsWith(prefix)) {
                String sequenceStr = maxTableCode.substring(prefix.length());
                try {
                    nextSequence = Integer.parseInt(sequenceStr) + 1;
                } catch (NumberFormatException ignored) {
                    nextSequence = 1;
                }
            }
        }
        return String.format("%sXJ%04d", username, nextSequence);
    }

    private String generateImplementer() {
        SysUser user = UserContext.getUser();
        if (user == null) throw new RuntimeException("用户信息不存在");
        String username = user.getUsername();
        String realName = user.getRealName();
        if (!StringUtils.hasText(username)) throw new RuntimeException("用户名不存在");
        return StringUtils.hasText(realName) ? username + "-" + realName : username;
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
