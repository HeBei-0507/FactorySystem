package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.idAddress.IDAddressAddDTO;
import com.hebei.systemdemo.domain.dto.idAddress.IDAddressEditDTO;
import com.hebei.systemdemo.domain.po.IDAddress;
import com.hebei.systemdemo.service.IIDAddressService;
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
@RequestMapping("/idAddress")
public class IDAddressController {
    @Autowired
    private IIDAddressService idAddressService;

    @RequireLogin
    @OperateLog(module = "ID位置管理", operation = "新增ID位置", saveParams = true, saveResult = false)
    @PostMapping("/addIDAddress")
    public Result addIDAddress(@Valid @RequestBody IDAddressAddDTO idAddressAddDTO) {
        IDAddress idAddress = BeanUtil.copyProperties(idAddressAddDTO, IDAddress.class);
        if (idAddress.getCreatorUsername() == null) {
            idAddress.setCreatorUsername(UserContext.getUsername());
        }
        return idAddressService.addIDAddress(idAddress);
    }

    @RequireLogin
    @OperateLog(module = "ID位置管理", operation = "修改ID位置", saveParams = true, saveResult = false)
    @PutMapping("/updateIDAddress")
    public Result updateIDAddress(@Valid @RequestBody IDAddressEditDTO idAddressEditDTO) {
        IDAddress idAddress = BeanUtil.copyProperties(idAddressEditDTO, IDAddress.class);
        return idAddressService.updateIDAddress(idAddress);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable("id") Long id) {
        return idAddressService.getById(id);
    }

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String idLocationCode,
                       @RequestParam(required = false) String idLocationName,
                       @RequestParam(required = false) String idLocationInnerCode,
                       @RequestParam(required = false) String locationType,
                       @RequestParam(required = false) String idLocationCategory) {
        return idAddressService.page(current, size, idLocationCode, idLocationName,
                idLocationInnerCode, locationType, idLocationCategory);
    }

    @RequireLogin
    @OperateLog(module = "ID位置管理", operation = "删除ID位置", saveParams = true, saveResult = false)
    @DeleteMapping("/deleteIDAddress/{id}")
    public Result deleteById(@PathVariable("id") Long id) {
        return idAddressService.deleteById(id);
    }

    @RequireLogin
    @OperateLog(module = "ID位置管理", operation = "批量删除ID位置", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDeleteIDAddress")
    public Result batchDeleteIDAddress(@RequestBody List<Long> ids) {
        return idAddressService.batchDeleteIDAddress(ids);
    }
}
