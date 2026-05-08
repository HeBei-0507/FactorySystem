package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.inspectionRoute.InspectionRouteAddDTO;
import com.hebei.systemdemo.domain.dto.inspectionRoute.InspectionRouteEditDTO;
import com.hebei.systemdemo.domain.po.InspectionRoute;
import com.hebei.systemdemo.service.IInspectionRouteService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/inspectionRoute")
public class InspectionRouteController {
    @Autowired
    private IInspectionRouteService inspectionRouteService;

    @RequireLogin
    @GetMapping("/list/{productionLineId}")
    public Result getInspectionRouteList(@PathVariable("productionLineId") Long productionLineId) {
        return inspectionRouteService.getInspectionRouteListByProductionLineId(productionLineId);
    }

    @RequireLogin
    @OperateLog(module = "点检路线管理", operation = "新增点检路线", saveParams = true, saveResult = false)
    @PostMapping("/addInspectionRoute")
    public Result addInspectionRoute(@Valid @RequestBody InspectionRouteAddDTO inspectionRouteAddDTO) {
        InspectionRoute inspectionRoute = BeanUtil.copyProperties(inspectionRouteAddDTO, InspectionRoute.class);
        inspectionRoute.setCreatorId(UserContext.getUserId());
        return inspectionRouteService.addInspectionRoute(inspectionRoute);
    }

    @RequireLogin
    @OperateLog(module = "点检路线管理", operation = "修改点检路线", saveParams = true, saveResult = false)
    @PutMapping("/updateInspectionRoute")
    public Result updateInspectionRoute(@Valid @RequestBody InspectionRouteEditDTO inspectionRouteEditDTO) {
        InspectionRoute inspectionRoute = BeanUtil.copyProperties(inspectionRouteEditDTO, InspectionRoute.class);
        return inspectionRouteService.updateInspectionRoute(inspectionRoute);
    }

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) Long productionLineId,
                       @RequestParam(required = false) String routeCode,
                       @RequestParam(required = false) String routeName) {
        return inspectionRouteService.page(current, size, productionLineId, routeCode, routeName);
    }

    @RequireLogin
    @OperateLog(module = "点检路线管理", operation = "批量删除点检路线", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDeleteInspectionRoute")
    public Result batchDeleteInspectionRoute(@RequestBody List<Long> ids) {
        return inspectionRouteService.batchDeleteInspectionRoute(ids);
    }
}
