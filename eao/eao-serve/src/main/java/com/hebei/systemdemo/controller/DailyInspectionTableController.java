package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.dailyInspectionTable.DailyInspectionTableAddDTO;
import com.hebei.systemdemo.domain.dto.dailyInspectionTable.DailyInspectionTableEditDTO;
import com.hebei.systemdemo.domain.po.DailyInspectionTable;
import com.hebei.systemdemo.service.IDailyInspectionTableService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/dailyInspectionTable")
public class DailyInspectionTableController {
    @Autowired
    private IDailyInspectionTableService dailyInspectionTableService;

    @RequireLogin
    @OperateLog(module = "日常巡检表管理", operation = "新增日常巡检表", saveParams = true, saveResult = false)
    @PostMapping("/addDailyInspectionTable")
    public Result addDailyInspectionTable(@Valid @RequestBody DailyInspectionTableAddDTO dailyInspectionTableAddDTO) {
        DailyInspectionTable dailyInspectionTable = BeanUtil.copyProperties(dailyInspectionTableAddDTO, DailyInspectionTable.class);
        dailyInspectionTable.setCreatorId(UserContext.getUserId());
        return dailyInspectionTableService.addDailyInspectionTable(dailyInspectionTable);
    }

    @RequireLogin
    @OperateLog(module = "日常巡检表管理", operation = "修改日常巡检表", saveParams = true, saveResult = false)
    @PutMapping("/updateDailyInspectionTable")
    public Result updateDailyInspectionTable(@Valid @RequestBody DailyInspectionTableEditDTO dailyInspectionTableEditDTO) {
        DailyInspectionTable dailyInspectionTable = BeanUtil.copyProperties(dailyInspectionTableEditDTO, DailyInspectionTable.class);
        return dailyInspectionTableService.updateDailyInspectionTable(dailyInspectionTable);
    }

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String productionLineName,
                       @RequestParam(required = false) String tableName,
                       @RequestParam(required = false) String tableCode,
                       @RequestParam(required = false) String shiftMode) {
        return dailyInspectionTableService.page(current, size, productionLineName, tableName, tableCode, shiftMode);
    }

    @RequireLogin
    @OperateLog(module = "日常巡检表管理", operation = "批量删除日常巡检表", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDeleteDailyInspectionTable")
    public Result batchDeleteDailyInspectionTable(@RequestBody List<Long> ids) {
        return dailyInspectionTableService.batchDeleteDailyInspectionTable(ids);
    }
}
