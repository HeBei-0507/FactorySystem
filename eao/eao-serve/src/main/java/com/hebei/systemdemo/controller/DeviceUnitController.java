package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.deviceUnit.DeviceUnitAddDTO;
import com.hebei.systemdemo.domain.dto.deviceUnit.DeviceUnitEditDTO;
import com.hebei.systemdemo.domain.dto.deviceUnit.DeviceUnitImportDTO;
import com.hebei.systemdemo.domain.po.DeviceUnit;
import com.hebei.systemdemo.service.IDeviceUnitService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/deviceUnit")
public class DeviceUnitController {
    @Autowired
    private IDeviceUnitService deviceUnitService;

    @RequireLogin
    @GetMapping("/getDeviceUtilList/{productionLineId}")
    public Result getDeviceUnitList(@PathVariable("productionLineId") Long productionLineId) {
        return deviceUnitService.getDeviceUnitListByProductionLineId(productionLineId);
    }

    @RequireLogin
    @OperateLog(module = "设备单元管理", operation = "新增设备单元", saveParams = true, saveResult = false)
    @PostMapping("/addDeviceUnit")
    public Result addDeviceUnit(@Valid @RequestBody DeviceUnitAddDTO deviceUnitAddDTO) {
        DeviceUnit deviceUnit = BeanUtil.copyProperties(deviceUnitAddDTO, DeviceUnit.class);
        deviceUnit.setCreatorId(UserContext.getUserId());
        return deviceUnitService.addDeviceUnit(deviceUnit);
    }

    @RequireLogin
    @OperateLog(module = "设备单元管理", operation = "批量添加设备单元", saveParams = true, saveResult = false)
    @PostMapping("/batchAddDeviceUnit")
    public Result batchAddDeviceUnit(@Valid @RequestBody DeviceUnitImportDTO deviceUnitImportDTO) {
        return deviceUnitService.batchAddDeviceUnit(deviceUnitImportDTO, UserContext.getUserId());
    }

    @RequireLogin
    @OperateLog(module = "设备单元管理", operation = "修改设备单元", saveParams = true, saveResult = false)
    @PutMapping("/updateDeviceUnit")
    public Result updateDeviceUnit(@Valid @RequestBody DeviceUnitEditDTO deviceUnitEditDTO) {
        DeviceUnit deviceUnit = BeanUtil.copyProperties(deviceUnitEditDTO, DeviceUnit.class);
        return deviceUnitService.updateDeviceUnit(deviceUnit);
    }

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) Long productionLineId,
                       @RequestParam(required = false) String unitCode,
                       @RequestParam(required = false) String unitName) {
        return deviceUnitService.page(current, size, productionLineId, unitCode, unitName);
    }

    @RequireLogin
    @OperateLog(module = "设备单元管理", operation = "批量删除设备单元", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDeleteDeviceUnit")
    public Result batchDeleteDeviceUnit(@RequestBody List<Long> ids) {
        return deviceUnitService.batchDeleteDeviceUnit(ids);
    }

}
