package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.Failure;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.FailureMapper;
import com.hebei.systemdemo.mapper.SysUserMapper;
import com.hebei.systemdemo.service.IFailureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class FailureService implements IFailureService {
    private static final String STATUS_PENDING_SUBMIT = "00";
    private static final String STATUS_PENDING_DEAL = "01";

    @Autowired
    private FailureMapper failureMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Result page(Integer current, Integer size, String failureCode, String devicePartCode, String failureName,
                       String failureStartTime, String status, String failureType) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<Failure> page = PageHelper.startPage(current, size);
        List<Failure> records = failureMapper.page(trimToNull(failureCode), trimToNull(devicePartCode), trimToNull(failureName), trimToNull(failureStartTime), trimToNull(status), trimToNull(failureType), currentUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    @Override
    public Result getById(Long id) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (id == null) return Result.fail(ResultCode.BAD_REQUEST, "故障记录ID不能为空");
        Failure failure = failureMapper.getById(id, currentUsername());
        return failure == null ? Result.fail(ResultCode.NOT_FOUND, "故障记录不存在或无权限访问") : Result.ok(failure);
    }

    @Override
    public Result add(Failure failure) {
        normalize(failure);
        fillCreatorInfo(failure);
        failure.setFailureCode(generateFailureCode());
        if (!StringUtils.hasText(failure.getStatus())) failure.setStatus(STATUS_PENDING_SUBMIT);
        String now = nowString();
        failure.setCreatedAt(now);
        failure.setUpdatedAt(now);
        int rows = failureMapper.add(failure);
        return rows > 0 && failure.getId() != null ? Result.ok(Collections.singletonMap("id", failure.getId())) : Result.fail("新增故障记录失败");
    }

    @Override
    public Result update(Failure failure) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (failure.getId() == null) return Result.fail(ResultCode.BAD_REQUEST, "故障记录ID不能为空");
        Failure exist = failureMapper.getById(failure.getId(), currentUsername());
        if (exist == null) return Result.fail(ResultCode.NOT_FOUND, "故障记录不存在或无权限修改");
        normalize(failure);
        failure.setUpdatedAt(nowString());
        return failureMapper.updateById(failure) > 0 ? Result.ok("修改成功", null) : Result.fail("修改故障记录失败");
    }

    @Override
    public Result deleteById(Long id) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (id == null) return Result.fail(ResultCode.BAD_REQUEST, "故障记录ID不能为空");
        Failure exist = failureMapper.getById(id, currentUsername());
        if (exist == null) return Result.fail(ResultCode.NOT_FOUND, "故障记录不存在或无权限删除");
        return failureMapper.deleteById(id) > 0 ? Result.ok("删除成功", Collections.singletonMap("deletedCount", 1)) : Result.fail("删除故障记录失败");
    }

    @Override
    public Result batchDelete(List<Long> ids) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail(ResultCode.BAD_REQUEST, "删除ID列表不能为空");
        for (Long id : ids) {
            if (failureMapper.getById(id, currentUsername()) == null) {
                return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的故障记录");
            }
        }
        int rows = failureMapper.batchDeleteByIds(ids);
        return rows > 0 ? Result.ok(String.format("成功删除%d条记录", rows), Collections.singletonMap("deletedCount", rows)) : Result.fail("批量删除故障记录失败");
    }

    @Override
    public Result submit(List<Long> ids) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (ids == null || ids.isEmpty()) return Result.fail(ResultCode.BAD_REQUEST, "提交ID列表不能为空");
        for (Long id : ids) {
            if (failureMapper.getById(id, currentUsername()) == null) {
                return Result.fail(ResultCode.NOT_FOUND, "存在不存在或无权限的故障记录");
            }
        }
        int rows = failureMapper.updateStatusByIds(ids, STATUS_PENDING_SUBMIT, STATUS_PENDING_DEAL, nowString());
        return rows > 0 ? Result.ok(String.format("成功提交%d条记录", rows), Collections.singletonMap("submittedCount", rows)) : Result.fail("未找到可提交的待提交故障记录");
    }

    private void normalize(Failure failure) {
        failure.setFailureCode(trimToNull(failure.getFailureCode()));
        failure.setDevicePartCode(trimToNull(failure.getDevicePartCode()));
        failure.setDevicePartName(trimToNull(failure.getDevicePartName()));
        failure.setFailureName(trimToNull(failure.getFailureName()));
        failure.setFailurePhenomenon(trimToNull(failure.getFailurePhenomenon()));
        failure.setFailureDescription(trimToNull(failure.getFailureDescription()));
        failure.setFailureNoticeType(trimToNull(failure.getFailureNoticeType()));
        failure.setFailureType(trimToNull(failure.getFailureType()));
        failure.setFailureStartTime(trimToNull(failure.getFailureStartTime()));
        failure.setStatus(trimToNull(failure.getStatus()));
        failure.setCreatorUsername(trimToNull(failure.getCreatorUsername()));
        failure.setCreatorName(trimToNull(failure.getCreatorName()));
    }

    private void fillCreatorInfo(Failure failure) {
        Long userId = UserContext.getUserId();
        if (userId == null) return;
        SysUser user = sysUserMapper.getById(userId);
        if (user == null) return;
        if (!StringUtils.hasText(failure.getCreatorUsername())) failure.setCreatorUsername(trimToNull(user.getUsername()));
        if (!StringUtils.hasText(failure.getCreatorName())) failure.setCreatorName(trimToNull(user.getRealName()));
    }

    private String currentUsername() {
        SysUser user = UserContext.getUser();
        return user == null ? null : trimToNull(user.getUsername());
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) return null;
        return value.trim();
    }

    private String nowString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String generateFailureCode() {
        return "GZ"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
