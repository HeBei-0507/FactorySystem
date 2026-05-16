package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.inspectionRecord.InspectionRecordAddDTO;
import com.hebei.systemdemo.domain.dto.inspectionRecord.InspectionRecordCompleteDTO;
import com.hebei.systemdemo.domain.dto.inspectionRecord.InspectionRecordEditDTO;
import com.hebei.systemdemo.domain.po.InspectionRecord;
import com.hebei.systemdemo.service.IInspectionRecordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inspectionRecord")
public class InspectionRecordController {
    @Autowired
    private IInspectionRecordService inspectionRecordService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) Long productionLineId,
                       @RequestParam(required = false) Long routeId,
                       @RequestParam(required = false) String planDateStart,
                       @RequestParam(required = false) String planDateEnd,
                       @RequestParam(required = false) String completionFlag,
                       @RequestParam(required = false) String partCode,
                       @RequestParam(required = false) String partName) {
        return inspectionRecordService.page(current, size, productionLineId, routeId, planDateStart, planDateEnd, completionFlag, partCode, partName);
    }

    @RequireLogin
    @GetMapping("/planSummaryPage")
    public Result planSummaryPage(@RequestParam Integer current,
                                  @RequestParam Integer size,
                                  @RequestParam(required = false) Long productionLineId,
                                  @RequestParam(required = false) String routeName,
                                  @RequestParam(required = false) String planSource) {
        return inspectionRecordService.planSummaryPage(current, size, productionLineId, routeName, planSource);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return inspectionRecordService.getById(id);
    }

    @RequireLogin
    @OperateLog(module = "点检实绩查询", operation = "新增点检实绩", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody InspectionRecordAddDTO addDTO) {
        InspectionRecord inspectionRecord = BeanUtil.copyProperties(addDTO, InspectionRecord.class);
        return inspectionRecordService.add(inspectionRecord);
    }

    @RequireLogin
    @OperateLog(module = "点检实绩查询", operation = "修改点检实绩", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody InspectionRecordEditDTO editDTO) {
        InspectionRecord inspectionRecord = BeanUtil.copyProperties(editDTO, InspectionRecord.class);
        return inspectionRecordService.update(inspectionRecord);
    }

    @RequireLogin
    @OperateLog(module = "点检实绩查询", operation = "删除点检实绩", saveParams = true, saveResult = false)
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return inspectionRecordService.deleteById(id);
    }

    @RequireLogin
    @OperateLog(module = "点检实绩查询", operation = "批量删除点检实绩", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids) {
        return inspectionRecordService.batchDelete(ids);
    }

    @RequireLogin
    @OperateLog(module = "点检实绩查询", operation = "完工点检实绩", saveParams = true, saveResult = false)
    @PutMapping("/complete")
    public Result complete(@Valid @RequestBody InspectionRecordCompleteDTO completeDTO) {
        return inspectionRecordService.complete(completeDTO.getIds());
    }
}
