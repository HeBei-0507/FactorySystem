package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.SupplierManage;

import java.util.List;

public interface ISupplierManageService {
    Result addSupplierManage(SupplierManage supplierManage);

    Result updateSupplierManage(SupplierManage supplierManage);

    Result getById(Long id);

    Result page(Integer current, Integer size, String supplierCode, String supplierName, String supplierCategory);

    Result deleteById(Long id);

    Result batchDeleteSupplierManage(List<Long> ids);
}
