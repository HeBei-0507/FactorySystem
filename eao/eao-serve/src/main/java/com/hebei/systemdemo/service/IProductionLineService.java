package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.po.ProductionLine;

public interface IProductionLineService {
    Result getProductionLineList();
    
    Result addProductionLine(ProductionLine productionLine);
    
    Result updateProductionLine(ProductionLine productionLine);
    
    Result deleteProductionLine(Long id);
    
    Result getProductionLineById(Long id);
}
