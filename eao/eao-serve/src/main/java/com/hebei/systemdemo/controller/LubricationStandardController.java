package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.lubricationStandard.LubricationStandardAddDTO;
import com.hebei.systemdemo.domain.dto.lubricationStandard.LubricationStandardEditDTO;
import com.hebei.systemdemo.domain.dto.lubricationStandard.LubricationStandardImportDTO;
import com.hebei.systemdemo.domain.po.LubricationStandard;
import com.hebei.systemdemo.service.ILubricationStandardService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/lubricationStandard")
public class LubricationStandardController {
    
    @Autowired
    private ILubricationStandardService lubricationStandardService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) Long productionLineId,
                       @RequestParam(required = false) Long deviceUnitId,
                       @RequestParam(required = false) Long equipmentId,
                       @RequestParam(required = false) String standardCode,
                       @RequestParam(required = false) String partCode,
                       @RequestParam(required = false) String partName,
                       @RequestParam(required = false) String feedPoint,
                       @RequestParam(required = false) String oilModels,
                       @RequestParam(required = false) String profession,
                       @RequestParam(required = false) String oilFeedType) {
        return lubricationStandardService.page(current, size, productionLineId, deviceUnitId, equipmentId, standardCode, partCode, partName,
                feedPoint, oilModels, profession, oilFeedType);
    }

    @RequireLogin
    @GetMapping("/standardList/{partCode}")
    public Result getStandardListByPartCode(@PathVariable("partCode") String partCode) {
        return lubricationStandardService.getStandardListByPartCode(partCode);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return lubricationStandardService.getById(id);
    }

    @RequireLogin
    @OperateLog(module = "润滑标准管理", operation = "新增润滑标准", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody LubricationStandardAddDTO addDTO) {
        LubricationStandard lubricationStandard = BeanUtil.copyProperties(addDTO, LubricationStandard.class);
        return lubricationStandardService.add(lubricationStandard);
    }

    @RequireLogin
    @OperateLog(module = "润滑标准管理", operation = "批量导入润滑标准", saveParams = true, saveResult = false)
    @PostMapping("/batchAdd")
    public Result batchAdd(@Valid @RequestBody LubricationStandardImportDTO importDTO) {
        return lubricationStandardService.batchAdd(importDTO, UserContext.getUserId());
    }

    @RequireLogin
    @OperateLog(module = "润滑标准管理", operation = "修改润滑标准", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody LubricationStandardEditDTO editDTO) {
        LubricationStandard lubricationStandard = BeanUtil.copyProperties(editDTO, LubricationStandard.class);
        return lubricationStandardService.update(lubricationStandard);
    }

    @RequireLogin
    @OperateLog(module = "润滑标准管理", operation = "删除润滑标准", saveParams = true, saveResult = false)
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return lubricationStandardService.deleteById(id);
    }
}
