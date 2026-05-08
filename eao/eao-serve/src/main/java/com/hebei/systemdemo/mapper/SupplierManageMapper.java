package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.SupplierManage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SupplierManageMapper {
    List<SupplierManage> list(SupplierManage supplierManage);

    int addSupplierManage(SupplierManage supplierManage);

    SupplierManage getById(@Param("id") Long id,
                           @Param("creatorId") Long creatorId);

    int updateSupplierManage(SupplierManage supplierManage);

    List<SupplierManage> page(@Param("supplierCode") String supplierCode,
                              @Param("supplierName") String supplierName,
                              @Param("supplierCategory") String supplierCategory,
                              @Param("creatorId") Long creatorId);

    int deleteById(@Param("id") Long id);

    int batchDeleteByIds(@Param("ids") List<Long> ids);

    SupplierManage getBySupplierCodeAndCreatorId(@Param("supplierCode") String supplierCode,
                                                 @Param("creatorId") Long creatorId);
}
