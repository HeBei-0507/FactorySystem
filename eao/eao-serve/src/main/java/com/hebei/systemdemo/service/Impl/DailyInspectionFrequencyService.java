package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.DailyInspectionFrequency;
import com.hebei.systemdemo.domain.po.DailyInspectionTable;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.DailyInspectionFrequencyMapper;
import com.hebei.systemdemo.mapper.DailyInspectionTableMapper;
import com.hebei.systemdemo.service.IDailyInspectionFrequencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyInspectionFrequencyService implements IDailyInspectionFrequencyService {
    @Autowired
    private DailyInspectionFrequencyMapper dailyInspectionFrequencyMapper;
    @Autowired
    private DailyInspectionTableMapper dailyInspectionTableMapper;

    @Override
    public Result addDailyInspectionFrequency(DailyInspectionFrequency dailyInspectionFrequency) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (dailyInspectionFrequency.getDailyInspectionTableId() == null) return Result.fail(ResultCode.BAD_REQUEST, "请选择所属日常巡检表");
        DailyInspectionTable parentTable = dailyInspectionTableMapper.getById(dailyInspectionFrequency.getDailyInspectionTableId(), currentUserId());
        if (parentTable == null) return Result.fail(ResultCode.NOT_FOUND, "所属日常巡检表不存在或无权限访问");

        dailyInspectionFrequency.setCreatorId(currentUserId());
        dailyInspectionFrequency.setCreatedAt(normalizeDateTime(dailyInspectionFrequency.getCreatedAt()));
        dailyInspectionFrequency.setUpdatedAt(normalizeDateTime(dailyInspectionFrequency.getUpdatedAt()));

        if (!dailyInspectionFrequencyMapper.list(new DailyInspectionFrequency()
                .setDailyInspectionTableId(dailyInspectionFrequency.getDailyInspectionTableId())
                .setFrequencyCode(trimToNull(dailyInspectionFrequency.getFrequencyCode()))
                .setCreatorId(currentUserId())).isEmpty()) {
            return Result.fail("当前日常巡检表下项次编号已存在");
        }

        int rows = dailyInspectionFrequencyMapper.addDailyInspectionFrequency(dailyInspectionFrequency);
        if (rows <= 0 || dailyInspectionFrequency.getId() == null) return Result.fail("新增日常巡检频次失败");
        return Result.ok(Collections.singletonMap("id", dailyInspectionFrequency.getId()));
    }

    @Override
    public Result updateDailyInspectionFrequency(DailyInspectionFrequency dailyInspectionFrequency) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (dailyInspectionFrequency.getId() == null) return Result.fail(ResultCode.BAD_REQUEST, "ID不能为空");

        DailyInspectionFrequency existFrequency = dailyInspectionFrequencyMapper.getById(dailyInspectionFrequency.getId(), currentUserId());
        if (existFrequency == null) return Result.fail(ResultCode.NOT_FOUND, "日常巡检频次不存在或无权限访问");

        dailyInspectionFrequency.setUpdatedAt(normalizeDateTime(dailyInspectionFrequency.getUpdatedAt()));
        int rows = dailyInspectionFrequencyMapper.updateDailyInspectionFrequency(dailyInspectionFrequency);
        if (rows <= 0) return Result.fail("更新日常巡检频次失败");

        return Result.ok("更新成功", null);
    }

    @Override
    public Result page(Integer current, Integer size, Long dailyInspectionTableId, String frequencyCode, String areaDeviceName) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        if (dailyInspectionTableId == null) return Result.ok(Collections.singletonMap("records", Collections.emptyList()));
        DailyInspectionTable parentTable = dailyInspectionTableMapper.getById(dailyInspectionTableId, currentUserId());
        if (parentTable == null) return Result.ok(Collections.singletonMap("records", Collections.emptyList()));

        String normalizedFrequencyCode = trimToNull(frequencyCode);
        String normalizedAreaDeviceName = trimToNull(areaDeviceName);

        Page<DailyInspectionFrequency> page = PageHelper.startPage(current, size);
        List<DailyInspectionFrequency> records = dailyInspectionFrequencyMapper.page(dailyInspectionTableId, normalizedFrequencyCode, normalizedAreaDeviceName, currentUserId());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result batchDeleteDailyInspectionFrequency(List<Long> ids) {
        if (currentUserId() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");

        List<Long> deletableIds = new ArrayList<>();
        for (Long id : ids) {
            DailyInspectionFrequency existFrequency = dailyInspectionFrequencyMapper.getById(id, currentUserId());
            if (existFrequency != null) deletableIds.add(id);
        }
        if (deletableIds.isEmpty()) return Result.fail("未找到可删除的日常巡检频次或无权限删除");

        int rows = dailyInspectionFrequencyMapper.batchDeleteByIds(deletableIds);
        if (rows <= 0) return Result.fail("删除日常巡检频次失败");

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
