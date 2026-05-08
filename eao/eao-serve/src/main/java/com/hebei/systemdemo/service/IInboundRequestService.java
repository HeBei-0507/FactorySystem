package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.InboundRequest;

import java.util.List;

public interface IInboundRequestService {
    Result page(Integer current, Integer size, Long productionLineId, String inboundNo, String materialCode, String materialName, String areaCode, String locationCode, String inboundStatus);
    Result add(InboundRequest inboundRequest, Long userId, String username, String realName);
    Result update(InboundRequest inboundRequest);
    Result confirmInbound(List<Long> ids);
    Result batchDelete(List<Long> ids);
}
