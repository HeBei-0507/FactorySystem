package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.MaintenanceStandardSafetyTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenanceStandardSafetyTagMapper {
    List<MaintenanceStandardSafetyTag> listByStandardId(@Param("standardId") Long standardId);

    int add(MaintenanceStandardSafetyTag item);

    int deleteByStandardId(@Param("standardId") Long standardId);
}
