package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.inboundRequest.InboundRequestAddDTO;
import com.hebei.systemdemo.domain.dto.inboundRequest.InboundRequestEditDTO;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.service.IInboundRequestService;
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
@RequestMapping("/inboundRequest")
public class InboundRequestController {
    @Autowired
    private IInboundRequestService inboundRequestService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) Long productionLineId,
                       @RequestParam(required = false) String inboundNo,
                       @RequestParam(required = false) String materialCode,
                       @RequestParam(required = false) String materialName,
                       @RequestParam(required = false) String areaCode,
                       @RequestParam(required = false) String locationCode,
                       @RequestParam(required = false) String inboundStatus) {
        return inboundRequestService.page(current, size, productionLineId, inboundNo, materialCode, materialName, areaCode, locationCode, inboundStatus);
    }

    @RequireLogin
    @OperateLog(module = "入库申请", operation = "新增入库申请", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody InboundRequestAddDTO dto) {
        SysUser user = UserContext.getUser();
        return inboundRequestService.add(BeanUtil.copyProperties(dto, com.hebei.systemdemo.domain.po.InboundRequest.class), user == null ? null : user.getId(), user == null ? null : user.getUsername(), user == null ? null : user.getRealName());
    }

    @RequireLogin
    @OperateLog(module = "入库申请", operation = "修改入库申请", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody InboundRequestEditDTO dto) {
        return inboundRequestService.update(BeanUtil.copyProperties(dto, com.hebei.systemdemo.domain.po.InboundRequest.class));
    }

    @RequireLogin
    @OperateLog(module = "入库申请", operation = "确认入库", saveParams = true, saveResult = false)
    @PutMapping("/confirmInbound")
    public Result confirmInbound(@RequestBody List<Long> ids) {
        return inboundRequestService.confirmInbound(ids);
    }

    @RequireLogin
    @OperateLog(module = "入库申请", operation = "批量删除入库申请", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids) {
        return inboundRequestService.batchDelete(ids);
    }
}
