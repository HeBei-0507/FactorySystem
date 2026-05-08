package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.inspectionStandard.InspectionStandardAddDTO;
import com.hebei.systemdemo.domain.dto.inspectionStandard.InspectionStandardEditDTO;
import com.hebei.systemdemo.domain.dto.inspectionStandard.InspectionStandardImportDTO;
import com.hebei.systemdemo.domain.po.InspectionStandard;
import com.hebei.systemdemo.service.IInspectionStandardService;
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

@RestController
@RequestMapping("/inspectionStandard")
public class InspectionStandardController {
    
    @Autowired
    private IInspectionStandardService inspectionStandardService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String inspectionName,
                       @RequestParam(required = false) String partCode,
                       @RequestParam(required = false) java.util.List<String> partCodes,
                       @RequestParam(required = false) String partName,
                       @RequestParam(required = false) String cycleUnit,
                       @RequestParam(required = false) String dataType,
                       @RequestParam(required = false) String signalType,
                       @RequestParam(required = false) String implementationMethod,
                       @RequestParam(required = false) String standardCategory,
                       @RequestParam(required = false) String unitOfMeasurement,
                       @RequestParam(required = false) String profession) {
        return inspectionStandardService.page(current, size, inspectionName, partCode, partCodes, partName, cycleUnit,
                dataType, signalType, implementationMethod, standardCategory, unitOfMeasurement, profession);
    }

    @RequireLogin
    @GetMapping("/standardList/{partCode}")
    public Result getStandardListByPartCode(@PathVariable("partCode") String partCode) {
        return inspectionStandardService.getStandardListByPartCode(partCode);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return inspectionStandardService.getById(id);
    }

    @RequireLogin
    @OperateLog(module = "点检标准管理", operation = "新增点检标准", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody InspectionStandardAddDTO addDTO) {
        InspectionStandard inspectionStandard = BeanUtil.copyProperties(addDTO, InspectionStandard.class);
        return inspectionStandardService.add(inspectionStandard);
    }

    @RequireLogin
    @OperateLog(module = "点检标准管理", operation = "批量导入点检标准", saveParams = true, saveResult = false)
    @PostMapping("/batchAdd")
    public Result batchAdd(@Valid @RequestBody InspectionStandardImportDTO importDTO) {
        return inspectionStandardService.batchAdd(importDTO, UserContext.getUserId());
    }

    @RequireLogin
    @OperateLog(module = "点检标准管理", operation = "修改点检标准", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody InspectionStandardEditDTO editDTO) {
        InspectionStandard inspectionStandard = BeanUtil.copyProperties(editDTO, InspectionStandard.class);
        return inspectionStandardService.update(inspectionStandard);
    }

    @RequireLogin
    @OperateLog(module = "点检标准管理", operation = "删除点检标准", saveParams = true, saveResult = false)
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return inspectionStandardService.deleteById(id);
    }
}
