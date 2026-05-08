package com.hebei.systemdemo.controller;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.domain.po.ProductionLine;
import com.hebei.systemdemo.service.IProductionLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productionLine")
public class ProductionLineController {
    @Autowired
    private IProductionLineService productionLineService;

    @RequireLogin
    @GetMapping("/getProductionLineList")
    public Result getProductionLineList() {
        return productionLineService.getProductionLineList();
    }

    @RequireLogin
    @PostMapping("/add")
    public Result addProductionLine(@RequestBody ProductionLine productionLine) {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            productionLine.setCreatorId(userId);
        }
        return productionLineService.addProductionLine(productionLine);
    }

    @RequireLogin
    @PutMapping("/update")
    public Result updateProductionLine(@RequestBody ProductionLine productionLine) {
        return productionLineService.updateProductionLine(productionLine);
    }

    @RequireLogin
    @DeleteMapping("/delete/{id}")
    public Result deleteProductionLine(@PathVariable Long id) {
        return productionLineService.deleteProductionLine(id);
    }

    @RequireLogin
    @GetMapping("/getById/{id}")
    public Result getProductionLineById(@PathVariable Long id) {
        return productionLineService.getProductionLineById(id);
    }
}
