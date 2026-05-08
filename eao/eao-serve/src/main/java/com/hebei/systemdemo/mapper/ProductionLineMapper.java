package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.ProductionLine;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductionLineMapper {
    List<ProductionLine> list();
    
    ProductionLine getById(Long id);
    
    int add(ProductionLine productionLine);
    
    int updateById(ProductionLine productionLine);
    
    int deleteById(Long id);
}
