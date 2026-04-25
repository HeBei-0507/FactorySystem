package com.hebei.systemdemo.controller;

import com.hebei.systemdemo.DTO.Result;
import com.hebei.systemdemo.pojo.DeviceUnit;
import com.hebei.systemdemo.service.IDeviceUnitService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/deviceUnit")
public class DeviceUnitController {
    @Autowired
    private IDeviceUnitService deviceUnitService;

    @GetMapping("/getDeviceUtilList/{productionLineId}")
    public Result getDeviceUnitList(@PathVariable("productionLineId") Long productionLineId) {
        return deviceUnitService.getDeviceUnitListByProductionLineId(productionLineId);
    }

    @PostMapping("/addDeviceUnit")
    public Result addDeviceUnit(@Valid @RequestBody DeviceUnit deviceUnit) {
//        log.info("新增设备单元: {}", deviceUnit);
        return deviceUnitService.addDeviceUnit(deviceUnit);
    }

    @PutMapping("/updateDeviceUnit")
    public Result updateDeviceUnit(@Valid @RequestBody DeviceUnit deviceUnit) {
        log.info("更新设备单元: {}", deviceUnit);
        return deviceUnitService.updateDeviceUnit(deviceUnit);
    }

    @DeleteMapping("/deleteDeviceUnit")
    public Result deleteDeviceUnit(@RequestBody List<Long> ids) {
        log.info("删除设备单元: {}", ids);
        return deviceUnitService.deleteDeviceUnit(ids);
    }

    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) Long productionLineId,
                       @RequestParam(required = false) String unitCode,
                       @RequestParam(required = false) String unitName) {
        return deviceUnitService.page(current, size, productionLineId, unitCode, unitName);
    }

}
