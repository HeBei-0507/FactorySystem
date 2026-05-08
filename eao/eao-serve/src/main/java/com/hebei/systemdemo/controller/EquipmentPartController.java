package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.equipmentPart.EquipmentPartAddDTO;
import com.hebei.systemdemo.domain.dto.equipmentPart.EquipmentPartEditDTO;
import com.hebei.systemdemo.domain.dto.equipmentPart.EquipmentPartImportDTO;
import com.hebei.systemdemo.domain.po.EquipmentPart;
import com.hebei.systemdemo.service.IEquipmentPartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/equipmentPart")
public class EquipmentPartController {

    @Autowired
    private IEquipmentPartService equipmentPartService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) Long equipmentId,
                       @RequestParam(required = false) String partCode,
                       @RequestParam(required = false) String partName,
                       @RequestParam(required = false) String maintainer,
                       @RequestParam(required = false) String partType,
                       @RequestParam(required = false) String repairStrategy,
                       @RequestParam(required = false) String importanceLevel,
                       @RequestParam(required = false) String repairTeam,
                       @RequestParam(required = false) String operateTeam) {
        return equipmentPartService.page(current, size, equipmentId, partCode, partName,
                maintainer, partType, repairStrategy, importanceLevel, repairTeam, operateTeam);
    }

    @RequireLogin
    @GetMapping("/partList/{equipmentId}")
    public Result getPartListByEquipmentId(@PathVariable("equipmentId") Long equipmentId) {
        return equipmentPartService.getPartListByEquipmentId(equipmentId);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return equipmentPartService.getById(id);
    }

    @RequireLogin
    @OperateLog(module = "设备部位管理", operation = "新增设备部位", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody EquipmentPartAddDTO addDTO) {
        EquipmentPart equipmentPart = BeanUtil.copyProperties(addDTO, EquipmentPart.class);
        equipmentPart.setCreatorId(UserContext.getUserId());
        return equipmentPartService.add(equipmentPart);
    }

    @RequireLogin
    @OperateLog(module = "设备部位管理", operation = "批量导入设备部位", saveParams = true, saveResult = false)
    @PostMapping("/batchAdd")
    public Result batchAdd(@Valid @RequestBody EquipmentPartImportDTO importDTO) {
        return equipmentPartService.batchAdd(importDTO, UserContext.getUserId());
    }

    @RequireLogin
    @OperateLog(module = "设备部位管理", operation = "修改设备部位", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody EquipmentPartEditDTO editDTO) {
        EquipmentPart equipmentPart = BeanUtil.copyProperties(editDTO, EquipmentPart.class);
        return equipmentPartService.update(equipmentPart);
    }

    @RequireLogin
    @OperateLog(module = "设备部位管理", operation = "删除设备部位", saveParams = true, saveResult = false)
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return equipmentPartService.deleteById(id);
    }
}
