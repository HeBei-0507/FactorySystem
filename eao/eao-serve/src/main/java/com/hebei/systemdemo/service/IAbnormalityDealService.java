package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.AbnormalityDeal;

public interface IAbnormalityDealService {
    Result page(Integer current, Integer size,
                String abnormalCode,
                String deviceUnitCode,
                String reporter,
                String reportDate,
                String status,
                String abnormalType);

    Result approvePage(Integer current, Integer size,
                       String abnormalCode,
                       String deviceUnitCode,
                       String reporter,
                       String reportDate,
                       String status,
                       String abnormalType);

    Result getByAbnormalityId(Long abnormalityId);

    Result process(AbnormalityDeal abnormalityDeal);

    Result approve(Long abnormalityId);

    Result rollback(AbnormalityDeal abnormalityDeal);

    Result rollbackFromApprove(Long abnormalityId);
}
