package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.IDAddress;

import java.util.List;

public interface IIDAddressService {
    Result addIDAddress(IDAddress idAddress);

    Result updateIDAddress(IDAddress idAddress);

    Result getById(Long id);

    Result page(Integer current, Integer size, String idLocationCode, String idLocationName,
                String idLocationInnerCode, String locationType, String idLocationCategory);

    Result deleteById(Long id);

    Result batchDeleteIDAddress(List<Long> ids);
}
