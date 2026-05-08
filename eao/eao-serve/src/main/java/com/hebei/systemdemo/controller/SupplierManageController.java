package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.supplierManage.SupplierManageAddDTO;
import com.hebei.systemdemo.domain.dto.supplierManage.SupplierManageEditDTO;
import com.hebei.systemdemo.domain.po.SupplierManage;
import com.hebei.systemdemo.service.ISupplierManageService;
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
@RequestMapping("/supplierManage")
public class SupplierManageController {
    @Autowired
    private ISupplierManageService supplierManageService;

    @RequireLogin
    @OperateLog(module = "供应商管理", operation = "新增供应商", saveParams = true, saveResult = false)
    @PostMapping("/addSupplierManage")
    public Result addSupplierManage(@Valid @RequestBody SupplierManageAddDTO supplierManageAddDTO) {
        SupplierManage supplierManage = BeanUtil.copyProperties(supplierManageAddDTO, SupplierManage.class);
        supplierManage.setCreatorId(UserContext.getUserId());
        supplierManage.setCreatorUsername(UserContext.getUsername());
        return supplierManageService.addSupplierManage(supplierManage);
    }

    @RequireLogin
    @OperateLog(module = "供应商管理", operation = "修改供应商", saveParams = true, saveResult = false)
    @PutMapping("/updateSupplierManage")
    public Result updateSupplierManage(@Valid @RequestBody SupplierManageEditDTO supplierManageEditDTO) {
        SupplierManage supplierManage = BeanUtil.copyProperties(supplierManageEditDTO, SupplierManage.class);
        return supplierManageService.updateSupplierManage(supplierManage);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable("id") Long id) {
        return supplierManageService.getById(id);
    }

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String supplierCode,
                       @RequestParam(required = false) String supplierName,
                       @RequestParam(required = false) String supplierCategory) {
        return supplierManageService.page(current, size, supplierCode, supplierName, supplierCategory);
    }

    @RequireLogin
    @OperateLog(module = "供应商管理", operation = "删除供应商", saveParams = true, saveResult = false)
    @DeleteMapping("/deleteSupplierManage/{id}")
    public Result deleteById(@PathVariable("id") Long id) {
        return supplierManageService.deleteById(id);
    }

    @RequireLogin
    @OperateLog(module = "供应商管理", operation = "批量删除供应商", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDeleteSupplierManage")
    public Result batchDeleteSupplierManage(@RequestBody List<Long> ids) {
        return supplierManageService.batchDeleteSupplierManage(ids);
    }
}
