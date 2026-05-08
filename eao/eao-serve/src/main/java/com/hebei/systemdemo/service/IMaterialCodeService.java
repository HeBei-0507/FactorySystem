package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.MaterialCode;

import java.util.List;

public interface IMaterialCodeService {
    Result addMaterialCode(MaterialCode materialCode);

    Result updateMaterialCode(MaterialCode materialCode);

    Result getById(Long id);

    Result page(Integer current, Integer size, String materialCode, String materialName,
                String materialMajorCategory, String materialSubCategory,
                String materialProperty, String status);

    Result deleteById(Long id);

    Result batchDeleteMaterialCode(List<Long> ids);
}
