package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.MaintenanceStandardSparePart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenanceStandardSparePartMapper {
    List<MaintenanceStandardSparePart> listByStandardId(@Param("standardId") Long standardId);

    int add(MaintenanceStandardSparePart item);

    int deleteByStandardId(@Param("standardId") Long standardId);
}
