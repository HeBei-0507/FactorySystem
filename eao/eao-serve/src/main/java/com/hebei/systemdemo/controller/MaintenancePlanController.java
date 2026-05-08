package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.maintenancePlan.MaintenancePlanAddDTO;
import com.hebei.systemdemo.domain.dto.maintenancePlan.MaintenancePlanEditDTO;
import com.hebei.systemdemo.domain.po.MaintenancePlan;
import com.hebei.systemdemo.service.IMaintenancePlanService;
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
@RequestMapping("/maintenancePlan")
public class MaintenancePlanController {
    @Autowired
    private IMaintenancePlanService maintenancePlanService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String planCode,
                       @RequestParam(required = false) String productionLineCode,
                       @RequestParam(required = false) String maintenanceCategory,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false, defaultValue = "false") Boolean excludePending) {
        return maintenancePlanService.page(current, size, planCode, productionLineCode, maintenanceCategory, status, excludePending);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return maintenancePlanService.getById(id);
    }

    @RequireLogin
    @OperateLog(module = "检修计划编制", operation = "新增检修计划", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody MaintenancePlanAddDTO dto) {
        MaintenancePlan maintenancePlan = BeanUtil.copyProperties(dto, MaintenancePlan.class);
        return maintenancePlanService.add(maintenancePlan);
    }

    @RequireLogin
    @OperateLog(module = "检修计划编制", operation = "修改检修计划", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody MaintenancePlanEditDTO dto) {
        MaintenancePlan maintenancePlan = BeanUtil.copyProperties(dto, MaintenancePlan.class);
        return maintenancePlanService.update(maintenancePlan);
    }

    @RequireLogin
    @OperateLog(module = "检修计划编制", operation = "批量删除检修计划", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids) {
        return maintenancePlanService.deleteByIds(ids);
    }

    @RequireLogin
    @OperateLog(module = "检修计划编制", operation = "批量送审检修计划", saveParams = true, saveResult = false)
    @PostMapping("/submit")
    public Result submit(@RequestBody List<Long> ids) {
        return maintenancePlanService.submitByIds(ids);
    }

    @RequireLogin
    @OperateLog(module = "检修计划审核", operation = "同意检修计划", saveParams = true, saveResult = false)
    @PostMapping("/approve")
    public Result approve(@RequestBody List<Long> ids) {
        return maintenancePlanService.approveByIds(ids);
    }

    @RequireLogin
    @OperateLog(module = "检修计划审核", operation = "回退检修计划", saveParams = true, saveResult = false)
    @PostMapping("/rollback")
    public Result rollback(@RequestBody List<Long> ids) {
        return maintenancePlanService.rollbackApproveByIds(ids);
    }
}
