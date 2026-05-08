package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.Abnormality;

import java.util.List;

public interface IAbnormalityService {
    Result page(Integer current, Integer size,
                String abnormalCode,
                String deviceUnitCode,
                String reporter,
                String reportDate,
                String status,
                String abnormalType);

    Result getById(Long id);

    Result add(Abnormality abnormality);

    Result update(Abnormality abnormality);

    Result deleteById(Long id);

    Result batchDelete(List<Long> ids);

    Result submit(List<Long> ids);
}
