package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.Failure;
import com.hebei.systemdemo.domain.po.FailureDeal;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.FailureDealMapper;
import com.hebei.systemdemo.mapper.FailureMapper;
import com.hebei.systemdemo.service.IFailureDealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FailureDealService implements IFailureDealService {
    private static final String STATUS_PENDING_DEAL = "01";
    private static final String STATUS_PENDING_APPROVE = "02";
    private static final String STATUS_APPROVED = "03";
    private static final String STATUS_PENDING_SUBMIT = "00";

    @Autowired
    private FailureDealMapper failureDealMapper;

    @Autowired
    private FailureMapper failureMapper;

    @Override
    public Result page(Integer current, Integer size, String failureCode, String devicePartCode, String failureName,
                       String failureStartTime, String status, String failureType) {
        return pageInternal(current, size, failureCode, devicePartCode, failureName, failureStartTime, status, failureType, false);
    }

    @Override
    public Result approvePage(Integer current, Integer size, String failureCode, String devicePartCode, String failureName,
                              String failureStartTime, String status, String failureType) {
        return pageInternal(current, size, failureCode, devicePartCode, failureName, failureStartTime, status, failureType, true);
    }

    @Override
    public Result getByFailureId(Long failureId) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (failureId == null) return Result.fail(ResultCode.BAD_REQUEST, "故障记录ID不能为空");
        Failure failure = failureMapper.getById(failureId, currentUsername());
        if (failure == null) return Result.fail(ResultCode.NOT_FOUND, "故障记录不存在或无权限访问");
        Map<String, Object> data = new HashMap<>();
        data.put("failure", failure);
        data.put("deal", failureDealMapper.getByFailureId(failureId, currentUsername()));
        return Result.ok(data);
    }

    @Override
    public Result process(FailureDeal failureDeal) {
        return saveAndFlow(failureDeal, STATUS_PENDING_DEAL, STATUS_PENDING_APPROVE, "处理成功");
    }

    @Override
    public Result approve(Long failureId) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (failureId == null) return Result.fail(ResultCode.BAD_REQUEST, "故障记录ID不能为空");
        Failure failure = failureMapper.getById(failureId, currentUsername());
        if (failure == null) return Result.fail(ResultCode.NOT_FOUND, "故障记录不存在或无权限访问");
        if (!STATUS_PENDING_APPROVE.equals(failure.getStatus())) return Result.fail("当前故障记录状态不允许执行该操作");
        failureMapper.updateById(new Failure().setId(failureId).setStatus(STATUS_APPROVED).setUpdatedAt(nowString()));
        return Result.ok("同意成功", null);
    }

    @Override
    public Result rollback(FailureDeal failureDeal) {
        return rollbackToInput(failureDeal == null ? null : failureDeal.getFailureId(), STATUS_PENDING_DEAL);
    }

    @Override
    public Result rollbackFromApprove(Long failureId) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (failureId == null) return Result.fail(ResultCode.BAD_REQUEST, "故障记录ID不能为空");
        Failure failure = failureMapper.getById(failureId, currentUsername());
        if (failure == null) return Result.fail(ResultCode.NOT_FOUND, "故障记录不存在或无权限访问");
        if (!STATUS_PENDING_APPROVE.equals(failure.getStatus()) && !STATUS_APPROVED.equals(failure.getStatus())) return Result.fail("当前故障记录状态不允许执行该操作");
        failureDealMapper.deleteByFailureId(failureId, currentUsername());
        failureMapper.updateById(new Failure().setId(failureId).setStatus(STATUS_PENDING_SUBMIT).setUpdatedAt(nowString()));
        return Result.ok("回退成功", null);
    }

    private Result pageInternal(Integer current, Integer size, String failureCode, String devicePartCode, String failureName,
                                String failureStartTime, String status, String failureType, boolean approve) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (current == null || current < 1) return Result.fail("当前页码必须大于等于1");
        if (size == null || size < 1) return Result.fail("每页条数必须大于等于1");
        Page<Failure> page = PageHelper.startPage(current, size);
        List<Failure> records = approve
                ? failureDealMapper.approvePage(trimToNull(failureCode), trimToNull(devicePartCode), trimToNull(failureName), trimToNull(failureStartTime), trimToNull(status), trimToNull(failureType), currentUsername())
                : failureDealMapper.page(trimToNull(failureCode), trimToNull(devicePartCode), trimToNull(failureName), trimToNull(failureStartTime), trimToNull(status), trimToNull(failureType), currentUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    private Result rollbackToInput(Long failureId, String expectedStatus) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (failureId == null) return Result.fail(ResultCode.BAD_REQUEST, "故障记录ID不能为空");
        Failure failure = failureMapper.getById(failureId, currentUsername());
        if (failure == null) return Result.fail(ResultCode.NOT_FOUND, "故障记录不存在或无权限访问");
        if (!expectedStatus.equals(failure.getStatus())) return Result.fail("当前故障记录状态不允许执行该操作");
        failureDealMapper.deleteByFailureId(failureId, currentUsername());
        failureMapper.updateById(new Failure().setId(failureId).setStatus(STATUS_PENDING_SUBMIT).setUpdatedAt(nowString()));
        return Result.ok("回退成功", null);
    }

    private Result saveAndFlow(FailureDeal failureDeal, String fromStatus, String toStatus, String successMessage) {
        if (currentUsername() == null) return Result.fail("未登录，请先登录");
        if (failureDeal == null || failureDeal.getFailureId() == null) return Result.fail(ResultCode.BAD_REQUEST, "故障记录ID不能为空");
        Failure failure = failureMapper.getById(failureDeal.getFailureId(), currentUsername());
        if (failure == null) return Result.fail(ResultCode.NOT_FOUND, "故障记录不存在或无权限访问");
        if (!fromStatus.equals(failure.getStatus())) return Result.fail("当前故障记录状态不允许执行该操作");
        normalize(failureDeal);
        String now = nowString();
        FailureDeal existing = failureDealMapper.getByFailureId(failureDeal.getFailureId(), currentUsername());
        if (existing == null) {
            failureDeal.setCreatedAt(now);
            failureDeal.setUpdatedAt(now);
            failureDealMapper.add(failureDeal);
        } else {
            failureDeal.setUpdatedAt(now);
            failureDealMapper.updateByFailureId(failureDeal);
        }
        failureMapper.updateById(new Failure().setId(failure.getId()).setStatus(toStatus).setUpdatedAt(now));
        return Result.ok(successMessage, null);
    }

    private void normalize(FailureDeal failureDeal) {
        failureDeal.setSeverityLevel(trimToNull(failureDeal.getSeverityLevel()));
        failureDeal.setProcessTime(trimToNull(failureDeal.getProcessTime()));
        failureDeal.setProcessDescription(trimToNull(failureDeal.getProcessDescription()));
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
}
