package com.hebei.systemdemo.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.domain.po.Abnormality;
import com.hebei.systemdemo.domain.po.AbnormalityDeal;
import com.hebei.systemdemo.domain.po.EquipmentPart;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.mapper.AbnormalityDealMapper;
import com.hebei.systemdemo.mapper.AbnormalityMapper;
import com.hebei.systemdemo.mapper.EquipmentPartMapper;
import com.hebei.systemdemo.service.IAbnormalityDealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AbnormalityDealService implements IAbnormalityDealService {
    private static final String STATUS_PENDING_DEAL = "10";
    private static final String STATUS_PROCESSED = "20";
    private static final String STATUS_APPROVED = "30";
    private static final String STATUS_PENDING_SUBMIT = "00";

    @Autowired
    private AbnormalityDealMapper abnormalityDealMapper;

    @Autowired
    private AbnormalityMapper abnormalityMapper;

    @Autowired
    private EquipmentPartMapper equipmentPartMapper;

    @Override
    public Result page(Integer current, Integer size, String abnormalCode, String deviceUnitCode, String reporter,
                       String reportDate, String status, String abnormalType) {
        return pageInternal(current, size, abnormalCode, deviceUnitCode, reporter, reportDate, status, abnormalType, false);
    }

    @Override
    public Result approvePage(Integer current, Integer size, String abnormalCode, String deviceUnitCode, String reporter,
                              String reportDate, String status, String abnormalType) {
        return pageInternal(current, size, abnormalCode, deviceUnitCode, reporter, reportDate, status, abnormalType, true);
    }

    @Override
    public Result getByAbnormalityId(Long abnormalityId) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (abnormalityId == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "异常记录ID不能为空");
        }
        Abnormality abnormality = abnormalityMapper.getById(abnormalityId, currentUsername());
        if (abnormality == null) {
            return Result.fail(ResultCode.NOT_FOUND, "异常记录不存在或无权限访问");
        }
        fillPartName(abnormality);
        AbnormalityDeal abnormalityDeal = abnormalityDealMapper.getByAbnormalityId(abnormalityId);
        Map<String, Object> data = new HashMap<>();
        data.put("abnormality", abnormality);
        data.put("deal", abnormalityDeal);
        return Result.ok(data);
    }

    @Override
    public Result process(AbnormalityDeal abnormalityDeal) {
        return saveAndFlow(abnormalityDeal, STATUS_PENDING_DEAL, STATUS_PROCESSED, "处理成功");
    }

    @Override
    public Result approve(Long abnormalityId) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (abnormalityId == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "异常记录ID不能为空");
        }
        Abnormality abnormality = abnormalityMapper.getById(abnormalityId, currentUsername());
        if (abnormality == null) {
            return Result.fail(ResultCode.NOT_FOUND, "异常记录不存在或无权限访问");
        }
        if (!STATUS_PROCESSED.equals(abnormality.getStatus())) {
            return Result.fail("当前异常记录状态不允许执行该操作");
        }
        abnormalityMapper.updateById(new Abnormality().setId(abnormalityId).setStatus(STATUS_APPROVED).setUpdatedAt(nowString()));
        return Result.ok("同意成功", null);
    }

    @Override
    public Result rollback(AbnormalityDeal abnormalityDeal) {
        return rollbackToInput(abnormalityDeal == null ? null : abnormalityDeal.getAbnormalityId(), STATUS_PENDING_DEAL);
    }

    @Override
    public Result rollbackFromApprove(Long abnormalityId) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (abnormalityId == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "异常记录ID不能为空");
        }
        Abnormality abnormality = abnormalityMapper.getById(abnormalityId, currentUsername());
        if (abnormality == null) {
            return Result.fail(ResultCode.NOT_FOUND, "异常记录不存在或无权限访问");
        }
        if (!STATUS_PROCESSED.equals(abnormality.getStatus()) && !STATUS_APPROVED.equals(abnormality.getStatus())) {
            return Result.fail("当前异常记录状态不允许执行该操作");
        }
        abnormalityDealMapper.deleteByAbnormalityId(abnormalityId);
        abnormalityMapper.updateById(new Abnormality().setId(abnormalityId).setStatus(STATUS_PENDING_SUBMIT).setUpdatedAt(nowString()));
        return Result.ok("回退成功", null);
    }

    private Result pageInternal(Integer current, Integer size, String abnormalCode, String deviceUnitCode, String reporter,
                                String reportDate, String status, String abnormalType, boolean approve) {
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
        List<Abnormality> records = approve
                ? abnormalityDealMapper.approvePage(trimToNull(abnormalCode), trimToNull(deviceUnitCode), trimToNull(reporter), trimToNull(reportDate), trimToNull(status), trimToNull(abnormalType), currentUsername())
                : abnormalityDealMapper.page(trimToNull(abnormalCode), trimToNull(deviceUnitCode), trimToNull(reporter), trimToNull(reportDate), trimToNull(status), trimToNull(abnormalType), currentUsername());
        records.forEach(this::fillPartName);
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", current);
        data.put("size", size);
        return Result.ok(data);
    }

    private Result rollbackToInput(Long abnormalityId, String expectedStatus) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (abnormalityId == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "异常记录ID不能为空");
        }
        Abnormality abnormality = abnormalityMapper.getById(abnormalityId, currentUsername());
        if (abnormality == null) {
            return Result.fail(ResultCode.NOT_FOUND, "异常记录不存在或无权限访问");
        }
        if (!expectedStatus.equals(abnormality.getStatus())) {
            return Result.fail("当前异常记录状态不允许执行该操作");
        }
        abnormalityDealMapper.deleteByAbnormalityId(abnormalityId);
        abnormalityMapper.updateById(new Abnormality().setId(abnormalityId).setStatus(STATUS_PENDING_SUBMIT).setUpdatedAt(nowString()));
        return Result.ok("回退成功", null);
    }

    private Result saveAndFlow(AbnormalityDeal abnormalityDeal, String fromStatus, String toStatus, String successMessage) {
        if (currentUsername() == null) {
            return Result.fail("未登录，请先登录");
        }
        if (abnormalityDeal == null || abnormalityDeal.getAbnormalityId() == null) {
            return Result.fail(ResultCode.BAD_REQUEST, "异常记录ID不能为空");
        }
        Abnormality abnormality = abnormalityMapper.getById(abnormalityDeal.getAbnormalityId(), currentUsername());
        if (abnormality == null) {
            return Result.fail(ResultCode.NOT_FOUND, "异常记录不存在或无权限访问");
        }
        if (!fromStatus.equals(abnormality.getStatus())) {
            return Result.fail("当前异常记录状态不允许执行该操作");
        }
        normalize(abnormalityDeal);
        String now = nowString();
        AbnormalityDeal existingDeal = abnormalityDealMapper.getByAbnormalityId(abnormalityDeal.getAbnormalityId());
        if (existingDeal == null) {
            abnormalityDeal.setCreatedAt(now);
            abnormalityDeal.setUpdatedAt(now);
            abnormalityDealMapper.add(abnormalityDeal);
        } else {
            abnormalityDeal.setUpdatedAt(now);
            abnormalityDealMapper.updateByAbnormalityId(abnormalityDeal);
        }
        abnormalityMapper.updateById(new Abnormality().setId(abnormality.getId()).setStatus(toStatus).setUpdatedAt(now));
        return Result.ok(successMessage, null);
    }

    private void fillPartName(Abnormality abnormality) {
        if (abnormality == null || !StringUtils.hasText(abnormality.getDeviceUnitCode())) {
            return;
        }
        List<EquipmentPart> parts = equipmentPartMapper.list(new EquipmentPart().setPartCode(abnormality.getDeviceUnitCode().trim()));
        if (!parts.isEmpty() && StringUtils.hasText(parts.get(0).getPartName())) {
            abnormality.setPartName(parts.get(0).getPartName().trim());
        }
    }

    private void normalize(AbnormalityDeal abnormalityDeal) {
        abnormalityDeal.setProcessor(trimToNull(abnormalityDeal.getProcessor()));
        abnormalityDeal.setProcessMethod(trimToNull(abnormalityDeal.getProcessMethod()));
        abnormalityDeal.setProcessDate(trimToNull(abnormalityDeal.getProcessDate()));
        abnormalityDeal.setReviewDate(trimToNull(abnormalityDeal.getReviewDate()));
        abnormalityDeal.setAbnormalOccurredDate(trimToNull(abnormalityDeal.getAbnormalOccurredDate()));
        abnormalityDeal.setProcessOpinion(trimToNull(abnormalityDeal.getProcessOpinion()));
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
}
