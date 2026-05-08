package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.MaintenanceTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenanceTicketMapper {
    List<MaintenanceTicket> page(@Param("ticketCode") String ticketCode,
                                 @Param("ticketName") String ticketName,
                                 @Param("status") String status,
                                 @Param("partCode") String partCode,
                                 @Param("partName") String partName,
                                 @Param("standardCode") String standardCode,
                                 @Param("planCode") String planCode,
                                 @Param("creatorId") Long creatorId);

    MaintenanceTicket getById(@Param("id") Long id,
                              @Param("creatorId") Long creatorId);

    int add(MaintenanceTicket ticket);

    int updateById(MaintenanceTicket ticket);

    int deleteByIds(@Param("ids") List<Long> ids,
                    @Param("allowedStatus") String allowedStatus,
                    @Param("creatorId") Long creatorId);

    int updateStatusByIds(@Param("ids") List<Long> ids,
                          @Param("fromStatus") String fromStatus,
                          @Param("toStatus") String toStatus,
                          @Param("updatedAt") String updatedAt,
                          @Param("creatorId") Long creatorId);
}
