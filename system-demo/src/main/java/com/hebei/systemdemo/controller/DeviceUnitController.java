package com.hebei.systemdemo.controller;

import com.hebei.systemdemo.DTO.Result;
import com.hebei.systemdemo.pojo.DeviceUnit;
import com.hebei.systemdemo.service.IDeviceUnitService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        log.info("新增设备单元: {}", deviceUnit);
        return deviceUnitService.addDeviceUnit(deviceUnit);
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
