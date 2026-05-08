package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.abnormalityDeal.AbnormalityDealHandleDTO;
import com.hebei.systemdemo.domain.po.AbnormalityDeal;
import com.hebei.systemdemo.service.IAbnormalityDealService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/abnormalityDeal")
public class AbnormalityDealController {
    @Autowired
    private IAbnormalityDealService abnormalityDealService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String abnormalCode,
                       @RequestParam(required = false) String deviceUnitCode,
                       @RequestParam(required = false) String reporter,
                       @RequestParam(required = false) String reportDate,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String abnormalType) {
        return abnormalityDealService.page(current, size, abnormalCode, deviceUnitCode, reporter, reportDate, status, abnormalType);
    }

    @RequireLogin
    @GetMapping("/approvePage")
    public Result approvePage(@RequestParam Integer current,
                              @RequestParam Integer size,
                              @RequestParam(required = false) String abnormalCode,
                              @RequestParam(required = false) String deviceUnitCode,
                              @RequestParam(required = false) String reporter,
                              @RequestParam(required = false) String reportDate,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String abnormalType) {
        return abnormalityDealService.approvePage(current, size, abnormalCode, deviceUnitCode, reporter, reportDate, status, abnormalType);
    }

    @RequireLogin
    @GetMapping("/{abnormalityId}")
    public Result getByAbnormalityId(@PathVariable Long abnormalityId) {
        return abnormalityDealService.getByAbnormalityId(abnormalityId);
    }

    @RequireLogin
    @OperateLog(module = "异常管理", operation = "处理异常记录", saveParams = true, saveResult = false)
    @PutMapping("/process")
    public Result process(@Valid @RequestBody AbnormalityDealHandleDTO dto) {
        return abnormalityDealService.process(BeanUtil.copyProperties(dto, AbnormalityDeal.class));
    }

    @RequireLogin
    @OperateLog(module = "异常管理", operation = "异常审核同意", saveParams = true, saveResult = false)
    @PutMapping("/approve/{abnormalityId}")
    public Result approve(@PathVariable Long abnormalityId) {
        return abnormalityDealService.approve(abnormalityId);
    }

    @RequireLogin
    @OperateLog(module = "异常管理", operation = "回退异常记录", saveParams = true, saveResult = false)
    @PutMapping("/rollback")
    public Result rollback(@Valid @RequestBody AbnormalityDealHandleDTO dto) {
        return abnormalityDealService.rollback(BeanUtil.copyProperties(dto, AbnormalityDeal.class));
    }

    @RequireLogin
    @OperateLog(module = "异常管理", operation = "审核回退异常记录", saveParams = true, saveResult = false)
    @PutMapping("/approveRollback/{abnormalityId}")
    public Result approveRollback(@PathVariable Long abnormalityId) {
        return abnormalityDealService.rollbackFromApprove(abnormalityId);
    }
}
