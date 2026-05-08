package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.failureDeal.FailureDealHandleDTO;
import com.hebei.systemdemo.domain.po.FailureDeal;
import com.hebei.systemdemo.service.IFailureDealService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/failureDeal")
public class FailureDealController {
    @Autowired
    private IFailureDealService failureDealService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String failureCode,
                       @RequestParam(required = false) String devicePartCode,
                       @RequestParam(required = false) String failureName,
                       @RequestParam(required = false) String failureStartTime,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String failureType) {
        return failureDealService.page(current, size, failureCode, devicePartCode, failureName, failureStartTime, status, failureType);
    }

    @RequireLogin
    @GetMapping("/approvePage")
    public Result approvePage(@RequestParam Integer current,
                              @RequestParam Integer size,
                              @RequestParam(required = false) String failureCode,
                              @RequestParam(required = false) String devicePartCode,
                              @RequestParam(required = false) String failureName,
                              @RequestParam(required = false) String failureStartTime,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String failureType) {
        return failureDealService.approvePage(current, size, failureCode, devicePartCode, failureName, failureStartTime, status, failureType);
    }

    @RequireLogin
    @GetMapping("/{failureId}")
    public Result getByFailureId(@PathVariable Long failureId) {
        return failureDealService.getByFailureId(failureId);
    }

    @RequireLogin
    @OperateLog(module = "故障管理", operation = "处理故障记录", saveParams = true, saveResult = false)
    @PutMapping("/process")
    public Result process(@Valid @RequestBody FailureDealHandleDTO dto) {
        return failureDealService.process(BeanUtil.copyProperties(dto, FailureDeal.class));
    }

    @RequireLogin
    @OperateLog(module = "故障管理", operation = "故障审核同意", saveParams = true, saveResult = false)
    @PutMapping("/approve/{failureId}")
    public Result approve(@PathVariable Long failureId) {
        return failureDealService.approve(failureId);
    }

    @RequireLogin
    @OperateLog(module = "故障管理", operation = "回退故障记录", saveParams = true, saveResult = false)
    @PutMapping("/rollback")
    public Result rollback(@Valid @RequestBody FailureDealHandleDTO dto) {
        return failureDealService.rollback(BeanUtil.copyProperties(dto, FailureDeal.class));
    }

    @RequireLogin
    @OperateLog(module = "故障管理", operation = "审核回退故障记录", saveParams = true, saveResult = false)
    @PutMapping("/approveRollback/{failureId}")
    public Result approveRollback(@PathVariable Long failureId) {
        return failureDealService.rollbackFromApprove(failureId);
    }
}
