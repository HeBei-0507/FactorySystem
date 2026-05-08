package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.Failure;

import java.util.List;

public interface IFailureService {
    Result page(Integer current, Integer size,
                String failureCode,
                String devicePartCode,
                String failureName,
                String failureStartTime,
                String status,
                String failureType);

    Result getById(Long id);

    Result add(Failure failure);

    Result update(Failure failure);

    Result deleteById(Long id);

    Result batchDelete(List<Long> ids);

    Result submit(List<Long> ids);
}
