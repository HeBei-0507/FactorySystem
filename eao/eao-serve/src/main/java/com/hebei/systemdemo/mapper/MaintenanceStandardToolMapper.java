package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.MaintenanceStandardTool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenanceStandardToolMapper {
    List<MaintenanceStandardTool> listByStandardId(@Param("standardId") Long standardId);

    int add(MaintenanceStandardTool item);

    int deleteByStandardId(@Param("standardId") Long standardId);
}
