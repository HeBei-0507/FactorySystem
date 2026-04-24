package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.pojo.ProductionLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductionLineMapper {
    @Select("select * from production_line")
    List<ProductionLine> list();
}
