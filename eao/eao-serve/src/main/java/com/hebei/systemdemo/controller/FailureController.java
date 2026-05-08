package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.failure.FailureAddDTO;
import com.hebei.systemdemo.domain.dto.failure.FailureEditDTO;
import com.hebei.systemdemo.domain.po.Failure;
import com.hebei.systemdemo.service.IFailureService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/failure")
public class FailureController {
    @Autowired
    private IFailureService failureService;

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
        return failureService.page(current, size, failureCode, devicePartCode, failureName, failureStartTime, status, failureType);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return failureService.getById(id);
    }

    @RequireLogin
    @OperateLog(module = "故障管理", operation = "新增故障记录", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody FailureAddDTO addDTO) {
        return failureService.add(BeanUtil.copyProperties(addDTO, Failure.class));
    }

    @RequireLogin
    @OperateLog(module = "故障管理", operation = "修改故障记录", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody FailureEditDTO editDTO) {
        return failureService.update(BeanUtil.copyProperties(editDTO, Failure.class));
    }

    @RequireLogin
    @OperateLog(module = "故障管理", operation = "删除故障记录", saveParams = true, saveResult = false)
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return failureService.deleteById(id);
    }

    @RequireLogin
    @OperateLog(module = "故障管理", operation = "批量删除故障记录", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids) {
        return failureService.batchDelete(ids);
    }

    @RequireLogin
    @OperateLog(module = "故障管理", operation = "提交故障记录", saveParams = true, saveResult = false)
    @PutMapping("/submit")
    public Result submit(@RequestBody List<Long> ids) {
        return failureService.submit(ids);
    }
}
