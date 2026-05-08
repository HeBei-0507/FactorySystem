package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.dailyInspectionFrequency.DailyInspectionFrequencyAddDTO;
import com.hebei.systemdemo.domain.dto.dailyInspectionFrequency.DailyInspectionFrequencyEditDTO;
import com.hebei.systemdemo.domain.po.DailyInspectionFrequency;
import com.hebei.systemdemo.service.IDailyInspectionFrequencyService;
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
@RequestMapping("/dailyInspectionFrequency")
public class DailyInspectionFrequencyController {
    @Autowired
    private IDailyInspectionFrequencyService dailyInspectionFrequencyService;

    @RequireLogin
    @OperateLog(module = "日常巡检频次管理", operation = "新增日常巡检频次", saveParams = true, saveResult = false)
    @PostMapping("/addDailyInspectionFrequency")
    public Result addDailyInspectionFrequency(@Valid @RequestBody DailyInspectionFrequencyAddDTO dailyInspectionFrequencyAddDTO) {
        DailyInspectionFrequency dailyInspectionFrequency = BeanUtil.copyProperties(dailyInspectionFrequencyAddDTO, DailyInspectionFrequency.class);
        dailyInspectionFrequency.setCreatorId(UserContext.getUserId());
        return dailyInspectionFrequencyService.addDailyInspectionFrequency(dailyInspectionFrequency);
    }

    @RequireLogin
    @OperateLog(module = "日常巡检频次管理", operation = "修改日常巡检频次", saveParams = true, saveResult = false)
    @PutMapping("/updateDailyInspectionFrequency")
    public Result updateDailyInspectionFrequency(@Valid @RequestBody DailyInspectionFrequencyEditDTO dailyInspectionFrequencyEditDTO) {
        DailyInspectionFrequency dailyInspectionFrequency = BeanUtil.copyProperties(dailyInspectionFrequencyEditDTO, DailyInspectionFrequency.class);
        return dailyInspectionFrequencyService.updateDailyInspectionFrequency(dailyInspectionFrequency);
    }

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) Long dailyInspectionTableId,
                       @RequestParam(required = false) String frequencyCode,
                       @RequestParam(required = false) String areaDeviceName) {
        return dailyInspectionFrequencyService.page(current, size, dailyInspectionTableId, frequencyCode, areaDeviceName);
    }

    @RequireLogin
    @OperateLog(module = "日常巡检频次管理", operation = "批量删除日常巡检频次", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDeleteDailyInspectionFrequency")
    public Result batchDeleteDailyInspectionFrequency(@RequestBody List<Long> ids) {
        return dailyInspectionFrequencyService.batchDeleteDailyInspectionFrequency(ids);
    }
}
