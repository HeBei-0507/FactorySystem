package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.warehouseLocation.WarehouseLocationAddDTO;
import com.hebei.systemdemo.domain.dto.warehouseLocation.WarehouseLocationEditDTO;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.domain.po.WarehouseLocation;
import com.hebei.systemdemo.service.IWarehouseLocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/warehouseLocation")
public class WarehouseLocationController {
    @Autowired
    private IWarehouseLocationService warehouseLocationService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) Long productionLineId,
                       @RequestParam(required = false) String areaCode,
                       @RequestParam(required = false) String locationCode,
                       @RequestParam(required = false) String keeperUsername,
                       @RequestParam(required = false) String keeperRealName) {
        return warehouseLocationService.page(current, size, productionLineId, areaCode, locationCode, keeperUsername, keeperRealName);
    }

    @RequireLogin
    @GetMapping("/listByProductionLine")
    public Result listByProductionLine(@RequestParam Long productionLineId) {
        return warehouseLocationService.listByProductionLine(productionLineId);
    }

    @RequireLogin
    @OperateLog(module = "库区库位管理", operation = "新增库区库位", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody WarehouseLocationAddDTO dto) {
        SysUser user = UserContext.getUser();
        return warehouseLocationService.add(BeanUtil.copyProperties(dto, WarehouseLocation.class), user == null ? null : user.getId(), user == null ? null : user.getUsername(), user == null ? null : user.getRealName());
    }

    @RequireLogin
    @OperateLog(module = "库区库位管理", operation = "修改库区库位", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody WarehouseLocationEditDTO dto) {
        return warehouseLocationService.update(BeanUtil.copyProperties(dto, WarehouseLocation.class));
    }

    @RequireLogin
    @OperateLog(module = "库区库位管理", operation = "批量删除库区库位", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids) {
        return warehouseLocationService.batchDelete(ids);
    }
}
