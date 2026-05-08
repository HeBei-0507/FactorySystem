package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.MaintenanceTicketSparePart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenanceTicketSparePartMapper {
    List<MaintenanceTicketSparePart> listByTicketId(@Param("ticketId") Long ticketId);

    int add(MaintenanceTicketSparePart item);

    int deleteByTicketId(@Param("ticketId") Long ticketId);
}
