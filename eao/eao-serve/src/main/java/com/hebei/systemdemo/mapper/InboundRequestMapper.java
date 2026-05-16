package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.InboundRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InboundRequestMapper {
    List<InboundRequest> page(@Param("productionLineId") Long productionLineId,
                              @Param("inboundNo") String inboundNo,
                              @Param("materialCode") String materialCode,
                              @Param("materialName") String materialName,
                              @Param("areaCode") String areaCode,
                              @Param("locationCode") String locationCode,
                              @Param("inboundStatus") String inboundStatus,
                              @Param("creatorId") Long creatorId);

    InboundRequest getById(@Param("id") Long id,
                           @Param("creatorId") Long creatorId);

    List<InboundRequest> listByInboundNo(@Param("inboundNo") String inboundNo,
                                         @Param("id") Long id);

    int add(InboundRequest inboundRequest);

    int updateById(@Param("request") InboundRequest inboundRequest,
                   @Param("creatorId") Long creatorId);

    int confirmByIds(@Param("ids") List<Long> ids,
                     @Param("updatedAt") String updatedAt,
                     @Param("creatorId") Long creatorId);

    int batchDeleteByIds(@Param("ids") List<Long> ids,
                         @Param("creatorId") Long creatorId);
}
