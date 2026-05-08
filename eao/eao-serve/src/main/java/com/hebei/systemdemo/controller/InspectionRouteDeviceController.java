package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.inspectionRouteDevice.InspectionRouteDeviceAddDTO;
import com.hebei.systemdemo.domain.dto.inspectionRouteDevice.InspectionRouteDeviceEditDTO;
import com.hebei.systemdemo.domain.po.InspectionRouteDevice;
import com.hebei.systemdemo.service.IInspectionRouteDeviceService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/inspectionRouteDevice")
public class InspectionRouteDeviceController {
    @Autowired
    private IInspectionRouteDeviceService inspectionRouteDeviceService;

    @RequireLogin
    @GetMapping("/list/{routeCode}")
    public Result getInspectionRouteDeviceList(@PathVariable("routeCode") String routeCode) {
        return inspectionRouteDeviceService.getInspectionRouteDeviceListByRouteCode(routeCode);
    }

    @RequireLogin
    @OperateLog(module = "路线点检设备管理", operation = "新增路线点检设备", saveParams = true, saveResult = false)
    @PostMapping("/addInspectionRouteDevice")
    public Result addInspectionRouteDevice(@Valid @RequestBody InspectionRouteDeviceAddDTO inspectionRouteDeviceAddDTO) {
        InspectionRouteDevice inspectionRouteDevice = BeanUtil.copyProperties(inspectionRouteDeviceAddDTO, InspectionRouteDevice.class);
        inspectionRouteDevice.setCreatorId(UserContext.getUserId());
        return inspectionRouteDeviceService.addInspectionRouteDevice(inspectionRouteDevice);
    }

    @RequireLogin
    @OperateLog(module = "路线点检设备管理", operation = "修改路线点检设备", saveParams = true, saveResult = false)
    @PutMapping("/updateInspectionRouteDevice")
    public Result updateInspectionRouteDevice(@Valid @RequestBody InspectionRouteDeviceEditDTO inspectionRouteDeviceEditDTO) {
        InspectionRouteDevice inspectionRouteDevice = BeanUtil.copyProperties(inspectionRouteDeviceEditDTO, InspectionRouteDevice.class);
        return inspectionRouteDeviceService.updateInspectionRouteDevice(inspectionRouteDevice);
    }

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String routeCode,
                       @RequestParam(required = false) String equipmentCode,
                       @RequestParam(required = false) String equipmentName) {
        return inspectionRouteDeviceService.page(current, size, routeCode, equipmentCode, equipmentName);
    }

    @RequireLogin
    @OperateLog(module = "路线点检设备管理", operation = "批量删除路线点检设备", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDeleteInspectionRouteDevice")
    public Result batchDeleteInspectionRouteDevice(@RequestBody List<Long> ids) {
        return inspectionRouteDeviceService.batchDeleteInspectionRouteDevice(ids);
    }
}
