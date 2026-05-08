package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.materialCode.MaterialCodeAddDTO;
import com.hebei.systemdemo.domain.dto.materialCode.MaterialCodeEditDTO;
import com.hebei.systemdemo.domain.po.MaterialCode;
import com.hebei.systemdemo.service.IMaterialCodeService;
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

import java.util.List;

@RestController
@RequestMapping("/materialCode")
public class MaterialCodeController {
    @Autowired
    private IMaterialCodeService materialCodeService;

    @RequireLogin
    @OperateLog(module = "物料代码管理", operation = "新增物料代码", saveParams = true, saveResult = false)
    @PostMapping("/addMaterialCode")
    public Result addMaterialCode(@Valid @RequestBody MaterialCodeAddDTO materialCodeAddDTO) {
        MaterialCode materialCode = BeanUtil.copyProperties(materialCodeAddDTO, MaterialCode.class);
        materialCode.setCreatorId(UserContext.getUserId());
        materialCode.setModifierId(UserContext.getUserId());
        return materialCodeService.addMaterialCode(materialCode);
    }

    @RequireLogin
    @OperateLog(module = "物料代码管理", operation = "修改物料代码", saveParams = true, saveResult = false)
    @PutMapping("/updateMaterialCode")
    public Result updateMaterialCode(@Valid @RequestBody MaterialCodeEditDTO materialCodeEditDTO) {
        MaterialCode materialCode = BeanUtil.copyProperties(materialCodeEditDTO, MaterialCode.class);
        materialCode.setModifierId(UserContext.getUserId());
        return materialCodeService.updateMaterialCode(materialCode);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable("id") Long id) {
        return materialCodeService.getById(id);
    }

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String materialCode,
                       @RequestParam(required = false) String materialName,
                       @RequestParam(required = false) String materialMajorCategory,
                       @RequestParam(required = false) String materialSubCategory,
                       @RequestParam(required = false) String materialProperty,
                       @RequestParam(required = false) String status) {
        return materialCodeService.page(current, size, materialCode, materialName,
                materialMajorCategory, materialSubCategory, materialProperty, status);
    }

    @RequireLogin
    @OperateLog(module = "物料代码管理", operation = "删除物料代码", saveParams = true, saveResult = false)
    @DeleteMapping("/deleteMaterialCode/{id}")
    public Result deleteById(@PathVariable("id") Long id) {
        return materialCodeService.deleteById(id);
    }

    @RequireLogin
    @OperateLog(module = "物料代码管理", operation = "批量删除物料代码", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDeleteMaterialCode")
    public Result batchDeleteMaterialCode(@RequestBody List<Long> ids) {
        return materialCodeService.batchDeleteMaterialCode(ids);
    }
}
