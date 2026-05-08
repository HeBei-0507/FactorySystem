package com.hebei.systemdemo.service.Impl;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.mapper.ProductionLineMapper;
import com.hebei.systemdemo.domain.po.ProductionLine;
import com.hebei.systemdemo.service.IProductionLineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductionLineService implements IProductionLineService {
    private static final Logger log = LoggerFactory.getLogger(ProductionLineService.class);

    @Autowired
    private ProductionLineMapper productionLineMapper;

    @Override
    public Result getProductionLineList() {
        List<ProductionLine> list = productionLineMapper.list();
        if(list == null || list.isEmpty()) {
            return Result.fail("未查询到数据");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("total", list.size());
        
        log.info("查询到生产线数量: {}", list.size());
        return Result.ok(data);
    }

    @Override
    public Result addProductionLine(ProductionLine productionLine) {
        productionLine.setCreateAt(normalizeDateTime(productionLine.getCreateAt()));
        productionLine.setUpdateAt(normalizeDateTime(productionLine.getUpdateAt()));
        int rows = productionLineMapper.add(productionLine);
        if (rows <= 0 || productionLine.getId() == null) {
            return Result.fail("新增生产线失败");
        }
        log.info("新增生产线成功，ID: {}", productionLine.getId());
        return Result.ok(Collections.singletonMap("id", productionLine.getId()));
    }

    @Override
    public Result updateProductionLine(ProductionLine productionLine) {
        if (productionLine.getId() == null) {
            return Result.fail("生产线ID不能为空");
        }
        productionLine.setUpdateAt(normalizeDateTime(productionLine.getUpdateAt()));
        int rows = productionLineMapper.updateById(productionLine);
        if (rows <= 0) {
            return Result.fail("更新生产线失败");
        }
        log.info("更新生产线成功，ID: {}", productionLine.getId());
        return Result.ok();
    }

    @Override
    public Result deleteProductionLine(Long id) {
        if (id == null) {
            return Result.fail("生产线ID不能为空");
        }
        int rows = productionLineMapper.deleteById(id);
        if (rows <= 0) {
            return Result.fail("删除生产线失败");
        }
        log.info("删除生产线成功，ID: {}", id);
        return Result.ok();
    }

    @Override
    public Result getProductionLineById(Long id) {
        if (id == null) {
            return Result.fail("生产线ID不能为空");
        }
        ProductionLine productionLine = productionLineMapper.getById(id);
        if (productionLine == null) {
            return Result.fail("未找到生产线信息");
        }
        return Result.ok(productionLine);
    }

    private LocalDateTime normalizeDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime : LocalDateTime.now();
    }
}
