package com.hebei.systemdemo.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.dto.maintenanceTicket.MaintenanceTicketAddDTO;
import com.hebei.systemdemo.domain.dto.maintenanceTicket.MaintenanceTicketEditDTO;
import com.hebei.systemdemo.domain.po.MaintenanceTicket;
import com.hebei.systemdemo.service.IMaintenanceTicketService;
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
@RequestMapping("/maintenanceTicket")
public class MaintenanceTicketController {
    @Autowired
    private IMaintenanceTicketService maintenanceTicketService;

    @RequireLogin
    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String ticketCode,
                       @RequestParam(required = false) String ticketName,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String partCode,
                       @RequestParam(required = false) String partName,
                       @RequestParam(required = false) String standardCode,
                       @RequestParam(required = false) String planCode) {
        return maintenanceTicketService.page(current, size, ticketCode, ticketName, status,
                partCode, partName, standardCode, planCode);
    }

    @RequireLogin
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return maintenanceTicketService.getById(id);
    }

    @RequireLogin
    @OperateLog(module = "工单维护", operation = "新增工单", saveParams = true, saveResult = false)
    @PostMapping("/add")
    public Result add(@Valid @RequestBody MaintenanceTicketAddDTO dto) {
        MaintenanceTicket ticket = BeanUtil.copyProperties(dto, MaintenanceTicket.class);
        return maintenanceTicketService.add(ticket, dto);
    }

    @RequireLogin
    @OperateLog(module = "工单维护", operation = "修改工单", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result update(@Valid @RequestBody MaintenanceTicketEditDTO dto) {
        MaintenanceTicket ticket = BeanUtil.copyProperties(dto, MaintenanceTicket.class);
        return maintenanceTicketService.update(ticket, dto);
    }

    @RequireLogin
    @OperateLog(module = "工单维护", operation = "批量删除工单", saveParams = true, saveResult = false)
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids) {
        return maintenanceTicketService.deleteByIds(ids);
    }

    @RequireLogin
    @OperateLog(module = "工单维护", operation = "批量送审工单", saveParams = true, saveResult = false)
    @PostMapping("/submit")
    public Result submit(@RequestBody List<Long> ids) {
        return maintenanceTicketService.submitByIds(ids);
    }

    @RequireLogin
    @OperateLog(module = "工单审核", operation = "批量同意工单", saveParams = true, saveResult = false)
    @PostMapping("/approve")
    public Result approve(@RequestBody List<Long> ids) {
        return maintenanceTicketService.approveByIds(ids);
    }

    @RequireLogin
    @OperateLog(module = "工单审核", operation = "批量回退工单", saveParams = true, saveResult = false)
    @PostMapping("/rollback")
    public Result rollback(@RequestBody List<Long> ids) {
        return maintenanceTicketService.rollbackApproveByIds(ids);
    }
}
