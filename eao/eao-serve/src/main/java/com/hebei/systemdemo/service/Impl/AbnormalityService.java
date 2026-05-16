package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.Abnormality;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.AbnormalityMapper;
import com.hebei.systemdemo.mapper.SysUserMapper;
import com.hebei.systemdemo.service.IAbnormalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AbnormalityService implements IAbnormalityService {
    private static final String STATUS_PENDING_SUBMIT = "00";
    private static final String STATUS_PENDING_DEAL = "10";

    @Autowired
    private AbnormalityMapper abnormalityMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Result page(Integer current, Integer size, String abnormalCode, String deviceUnitCode, String reporter,
                       String reportDate, String status, String abnormalType) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (current == null || current < 1) {
            return Result.fail("当前页码必须大于等于1");
        }
        if (size == null || size < 1) {
            return Result.fail("每页条数必须大于等于1");
        }
        Page<Abnormality> page = PageHelper.startPage(current, size);
        List<Abnormality> records = abnormalityMapper.page(
                trimToNull(abnormalCode),
                trimToNull(deviceUnitCode),
                trimToNull(reporter),
                trimToNull(reportDate),
                trimToNull(status),
                trimToNull(abnormalType),
                currentUsername()
        );
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result getById(Long id) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (id == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "异常记录ID不能为空");
        }
        Abnormality abnormality = abnormalityMapper.getById(id, currentUsername());
        if (abnormality == null) {
            return Result.fail(ResultCode.NOT_FOUND, "异常记录不存在或无权限访问");
        }
        return Result.ok(abnormality);
    }

    @Override
    public Result add(Abnormality abnormality) {
        normalize(abnormality);
        fillCreatorInfo(abnormality);
        abnormality.setAbnormalCode(generateAbnormalCode());
        if (!StringUtils.hasText(abnormality.getReporter())) {
            abnormality.setReporter(abnormality.getCreatorName());
        }
        if (!StringUtils.hasText(abnormality.getSource())) {
            abnormality.setSource("手工录入");
        }
        if (!StringUtils.hasText(abnormality.getReportDate())) {
            abnormality.setReportDate(LocalDate.now().toString());
        }
        if (!StringUtils.hasText(abnormality.getStatus())) {
            abnormality.setStatus(STATUS_PENDING_SUBMIT);
        }
        String now = nowString();
        abnormality.setCreatedAt(now);
        abnormality.setUpdatedAt(now);
        int rows = abnormalityMapper.add(abnormality);
        if (rows <= 0 || abnormality.getId() == null) {
            return Result.fail("新增异常记录失败");
        }
        return Result.ok(Collections.singletonMap("id", abnormality.getId()));
    }

    @Override
    public Result update(Abnormality abnormality) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (abnormality.getId() == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "异常记录ID不能为空");
        }
        Abnormality exist = abnormalityMapper.getById(abnormality.getId(), currentUsername());
        if (exist == null) {
            return Result.fail(ResultCode.NOT_FOUND, "异常记录不存在或无权限修改");
        }
        normalize(abnormality);
        abnormality.setUpdatedAt(nowString());
        int rows = abnormalityMapper.updateById(abnormality);
        if (rows <= 0) {
            return Result.fail("修改异常记录失败");
        }
        return Result.ok("修改成功", null);
    }

    @Override
    public Result deleteById(Long id) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (id == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "异常记录ID不能为空");
        }
        Abnormality exist = abnormalityMapper.getById(id, currentUsername());
        if (exist == null) {
            return Result.fail(ResultCode.NOT_FOUND, "异常记录不存在或无权限删除");
        }
        int rows = abnormalityMapper.deleteById(id);
        if (rows <= 0) {
            return Result.fail("删除异常记录失败");
        }
        return Result.ok("删除成功", Collections.singletonMap("deletedCount", rows));
    }

    @Override
    public Result batchDelete(List<Long> ids) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");
        }
        for (Long id : ids) {
            Abnormality exist = abnormalityMapper.getById(id, currentUsername());
            if (exist == null) {
                return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的异常记录");
            }
        }
        int rows = abnormalityMapper.batchDeleteByIds(ids);
        if (rows <= 0) {
            return Result.fail("批量删除异常记录失败");
        }
        return Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows));
    }

    @Override
    public Result submit(List<Long> ids) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ResultCode.BAD_REQUEST, "提交ID列表不能为空");
        }
        for (Long id : ids) {
            Abnormality exist = abnormalityMapper.getById(id, currentUsername());
            if (exist == null) {
                return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的异常记录");
            }
        }
        int rows = abnormalityMapper.updateStatusByIds(ids, STATUS_PENDING_SUBMIT, STATUS_PENDING_DEAL, nowString());
        if (rows <= 0) {
            return Result.fail("未找到可提交的待提交异常记录");
        }
        return Result.ok(String.format("成功提交%d条记录", rows), Collections.singletonMap("submittedCount", rows));
    }

    private void normalize(Abnormality abnormality) {
        abnormality.setAbnormalCode(trimToNull(abnormality.getAbnormalCode()));
        abnormality.setDeviceUnitCode(trimToNull(abnormality.getDeviceUnitCode()));
        abnormality.setSource(trimToNull(abnormality.getSource()));
        abnormality.setAbnormalLocation(trimToNull(abnormality.getAbnormalLocation()));
        abnormality.setAbnormalType(trimToNull(abnormality.getAbnormalType()));
        abnormality.setSafetyOutput(trimToNull(abnormality.getSafetyOutput()));
        abnormality.setReporter(trimToNull(abnormality.getReporter()));
        abnormality.setAbnormalDescription(trimToNull(abnormality.getAbnormalDescription()));
        abnormality.setReportDate(trimToNull(abnormality.getReportDate()));
        abnormality.setStatus(trimToNull(abnormality.getStatus()));
        abnormality.setHandler(trimToNull(abnormality.getHandler()));
        abnormality.setHandlerName(trimToNull(abnormality.getHandlerName()));
        abnormality.setHandlerDate(trimToNull(abnormality.getHandlerDate()));
        abnormality.setCreatorUsername(trimToNull(abnormality.getCreatorUsername()));
        abnormality.setCreatorName(trimToNull(abnormality.getCreatorName()));
    }

    private void fillCreatorInfo(Abnormality abnormality) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return;
        }
        SysUser user = sysUserMapper.getById(userId);
        if (user == null) {
            return;
        }
        if (!StringUtils.hasText(abnormality.getCreatorUsername())) {
            abnormality.setCreatorUsername(trimToNull(user.getUsername()));
        }
        if (!StringUtils.hasText(abnormality.getCreatorName())) {
            abnormality.setCreatorName(trimToNull(user.getRealName()));
        }
    }

    private String currentUsername() {
        SysUser user = UserContext.getUser();
        return user == null ? null : trimToNull(user.getUsername());
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String nowString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String generateAbnormalCode() {
        return "YC"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
