package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.FailureDeal;

public interface IFailureDealService {
    Result page(Integer current, Integer size,
                String failureCode,
                String devicePartCode,
                String failureName,
                String failureStartTime,
                String status,
                String failureType);

    Result approvePage(Integer current, Integer size,
                       String failureCode,
                       String devicePartCode,
                       String failureName,
                       String failureStartTime,
                       String status,
                       String failureType);

    Result getByFailureId(Long failureId);

    Result process(FailureDeal failureDeal);

    Result approve(Long failureId);

    Result rollback(FailureDeal failureDeal);

    Result rollbackFromApprove(Long failureId);
}
