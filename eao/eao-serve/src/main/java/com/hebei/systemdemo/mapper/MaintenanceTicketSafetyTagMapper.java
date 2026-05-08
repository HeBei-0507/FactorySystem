package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.MaintenanceTicketSafetyTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenanceTicketSafetyTagMapper {
    List<MaintenanceTicketSafetyTag> listByTicketId(@Param("ticketId") Long ticketId);

    int add(MaintenanceTicketSafetyTag item);

    int deleteByTicketId(@Param("ticketId") Long ticketId);
}
