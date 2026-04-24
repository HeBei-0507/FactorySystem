package com.hebei.systemdemo.service.Impl;

import com.hebei.systemdemo.DTO.Result;
import com.hebei.systemdemo.mapper.ProductionLineMapper;
import com.hebei.systemdemo.pojo.ProductionLine;
import com.hebei.systemdemo.service.IProductionLineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductionLineService implements IProductionLineService {
    private static final Logger log = LoggerFactory.getLogger(ProductionLineService.class);

    @Autowired
    private ProductionLineMapper productionLineMapper;

    @Override
    public Result getProductionLineList() {
        List<ProductionLine> list = productionLineMapper.list();
        Long total = (long)productionLineMapper.list().size();
        log.info("查询到生产线数量: {}", list == null ? null : list.size());
        if(list == null)
            return Result.fail("未查询到数据");
        return Result.ok(list, total);
    }
}
