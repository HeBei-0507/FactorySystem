package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.MaintenanceTicketTool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenanceTicketToolMapper {
    List<MaintenanceTicketTool> listByTicketId(@Param("ticketId") Long ticketId);

    int add(MaintenanceTicketTool item);

    int deleteByTicketId(@Param("ticketId") Long ticketId);
}
