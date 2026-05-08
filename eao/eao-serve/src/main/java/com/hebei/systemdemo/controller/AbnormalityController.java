package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.abnormality.AbnormalityAddDTO;
import com.hebei.systemdemo.domain.dto.abnormality.AbnormalityEditDTO;
import com.hebei.systemdemo.domain.po.Abnormality;
import com.hebei.systemdemo.service.IAbnormalityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/abnormality")
public class AbnormalityController {
    @Autowired
    private IAbnormalityService abnormalityService;

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
        return abnormalityService.page(current, size, abnormalCode, deviceUnitCode, reporter, reportDate, status, abnormalType);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return abnormalityService.getById(id);
    }

    @RequireLogin
    @OperateLog(module = "异常管理", operation = "新增异常记录", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody AbnormalityAddDTO addDTO) {
        Abnormality abnormality = BeanUtil.copyProperties(addDTO, Abnormality.class);
        return abnormalityService.add(abnormality);
    }

    @RequireLogin
    @OperateLog(module = "异常管理", operation = "修改异常记录", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody AbnormalityEditDTO editDTO) {
        Abnormality abnormality = BeanUtil.copyProperties(editDTO, Abnormality.class);
        return abnormalityService.update(abnormality);
    }

    @RequireLogin
    @OperateLog(module = "异常管理", operation = "删除异常记录", saveParams = true, saveResult = false)
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return abnormalityService.deleteById(id);
    }

    @RequireLogin
    @OperateLog(module = "异常管理", operation = "批量删除异常记录", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids) {
        return abnormalityService.batchDelete(ids);
    }

    @RequireLogin
    @OperateLog(module = "异常管理", operation = "提交异常记录", saveParams = true, saveResult = false)
    @PutMapping("/submit")
    public Result submit(@RequestBody List<Long> ids) {
        return abnormalityService.submit(ids);
    }
}
