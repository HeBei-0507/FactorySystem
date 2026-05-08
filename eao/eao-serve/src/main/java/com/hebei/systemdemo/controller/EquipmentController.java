package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.equipment.EquipmentAddDTO;
import com.hebei.systemdemo.domain.dto.equipment.EquipmentEditDTO;
import com.hebei.systemdemo.domain.dto.equipment.EquipmentImportDTO;
import com.hebei.systemdemo.domain.po.Equipment;
import com.hebei.systemdemo.service.IEquipmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipment")
public class EquipmentController {
    @Autowired
    private IEquipmentService equipmentService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) Long deviceUnitId,
                       @RequestParam(required = false) String equipmentCode,
                       @RequestParam(required = false) String equipmentName,
                       @RequestParam(required = false) String equipmentCategory,
                       @RequestParam(required = false) String equipmentImportance,
                       @RequestParam(required = false) String repairStrategy,
                       @RequestParam(required = false) String overhaulTeam,
                       @RequestParam(required = false) String actTeam) {
        return equipmentService.page(current, size, deviceUnitId, equipmentCode, equipmentName,
                equipmentCategory, equipmentImportance, repairStrategy, overhaulTeam, actTeam);
    }

    @RequireLogin
    @GetMapping("/equipmentList/{deviceUnitId}")
    public Result getEquipmentListByDeviceUnitId(@PathVariable("deviceUnitId") Long deviceUnitId) {
        return equipmentService.getEquipmentListByDeviceUnitId(deviceUnitId);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return equipmentService.getById(id);
    }

    @RequireLogin
    @GetMapping("/line/{lineCode}")
    public Result pageByLineCode(@PathVariable String lineCode,
                                 @RequestParam Integer current,
                                 @RequestParam Integer size,
                                 @RequestParam(required = false) String equipmentCode,
                                 @RequestParam(required = false) String equipmentName,
                                 @RequestParam(required = false) String equipmentCategory,
                                 @RequestParam(required = false) String equipmentImportance,
                                 @RequestParam(required = false) String repairStrategy,
                                 @RequestParam(required = false) String overhaulTeam,
                                 @RequestParam(required = false) String actTeam) {
        return equipmentService.pageByLineCode(current, size, lineCode, equipmentCode, equipmentName,
                equipmentCategory, equipmentImportance, repairStrategy, overhaulTeam, actTeam);
    }

    @RequireLogin
    @PostMapping("/add")
    public Result add(@Valid @RequestBody EquipmentAddDTO equipmentAddDTO) {
        Equipment equipment = BeanUtil.copyProperties(equipmentAddDTO, Equipment.class);
        equipment.setCreatorId(UserContext.getUserId());
        return equipmentService.add(equipment);
    }

    @RequireLogin
    @PostMapping("/batchAdd")
    public Result batchAdd(@Valid @RequestBody EquipmentImportDTO equipmentImportDTO) {
        return equipmentService.batchAdd(equipmentImportDTO, UserContext.getUserId());
    }

    @RequireLogin
    @DeleteMapping("/batchDel")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        return equipmentService.deleteBatch(ids);
    }

    @RequireLogin
    @PutMapping("/update")
    public Result update(@Valid @RequestBody EquipmentEditDTO equipmentEditDTO) {
        Equipment equipment = BeanUtil.copyProperties(equipmentEditDTO, Equipment.class);
        return equipmentService.update(equipment);
    }

    @RequireLogin
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return equipmentService.deleteById(id);
    }
}
