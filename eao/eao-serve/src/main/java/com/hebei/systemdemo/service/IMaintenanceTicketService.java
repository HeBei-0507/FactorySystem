package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.dto.maintenanceTicket.MaintenanceTicketAddDTO;
import com.hebei.systemdemo.domain.po.MaintenanceTicket;

import java.util.List;

public interface IMaintenanceTicketService {
    Result page(Integer current, Integer size, String ticketCode, String ticketName, String status,
                String partCode, String partName, String standardCode, String planCode);

    Result getById(Long id);

    Result add(MaintenanceTicket ticket, MaintenanceTicketAddDTO dto);

    Result update(MaintenanceTicket ticket, MaintenanceTicketAddDTO dto);

    Result deleteByIds(List<Long> ids);

    Result submitByIds(List<Long> ids);

    Result approveByIds(List<Long> ids);

    Result rollbackApproveByIds(List<Long> ids);
}
