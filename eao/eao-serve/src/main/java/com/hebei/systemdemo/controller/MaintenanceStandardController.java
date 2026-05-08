package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.maintenanceStandard.MaintenanceStandardAddDTO;
import com.hebei.systemdemo.domain.dto.maintenanceStandard.MaintenanceStandardEditDTO;
import com.hebei.systemdemo.domain.po.MaintenanceStandard;
import com.hebei.systemdemo.service.IMaintenanceStandardService;
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
@RequestMapping("/maintenanceStandard")
public class MaintenanceStandardController {
    @Autowired
    private IMaintenanceStandardService maintenanceStandardService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String standardCode,
                       @RequestParam(required = false) String partCode,
                       @RequestParam(required = false) String partName,
                       @RequestParam(required = false) String itemName,
                       @RequestParam(required = false) String profession,
                       @RequestParam(required = false) String maintenanceCategory,
                       @RequestParam(required = false) String maintenanceStartTime,
                       @RequestParam(required = false) String maintenanceEndTime,
                       @RequestParam(required = false) String workCategory,
                       @RequestParam(required = false) String workContent,
                       @RequestParam(required = false) List<String> partCodes) {
        return maintenanceStandardService.page(current, size, standardCode, partCode, partName, itemName,
                profession, maintenanceCategory, maintenanceStartTime, maintenanceEndTime,
                workCategory, workContent, partCodes);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return maintenanceStandardService.getById(id);
    }

    @RequireLogin
    @GetMapping("/materialCandidate/page")
    public Result materialCandidatePage(@RequestParam Integer current,
                                        @RequestParam Integer size,
                                        @RequestParam(required = false) String materialCode,
                                        @RequestParam(required = false) String materialName,
                                        @RequestParam(required = false) String materialSubCategory,
                                        @RequestParam(required = false, defaultValue = "10-已确认入库") String inboundStatus) {
        return maintenanceStandardService.getMaterialCandidates(current, size, materialCode, materialName,
                materialSubCategory, inboundStatus);
    }

    @RequireLogin
    @OperateLog(module = "检修标准项目管理", operation = "新增检修标准", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody MaintenanceStandardAddDTO dto) {
        MaintenanceStandard maintenanceStandard = BeanUtil.copyProperties(dto, MaintenanceStandard.class);
        return maintenanceStandardService.add(maintenanceStandard, dto);
    }

    @RequireLogin
    @OperateLog(module = "检修标准项目管理", operation = "修改检修标准", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody MaintenanceStandardEditDTO dto) {
        MaintenanceStandard maintenanceStandard = BeanUtil.copyProperties(dto, MaintenanceStandard.class);
        return maintenanceStandardService.update(maintenanceStandard, dto);
    }

    @RequireLogin
    @OperateLog(module = "检修标准项目管理", operation = "删除检修标准", saveParams = true, saveResult = false)
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return maintenanceStandardService.deleteById(id);
    }
}
