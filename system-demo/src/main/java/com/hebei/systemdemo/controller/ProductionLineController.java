package com.hebei.systemdemo.controller;

import com.hebei.systemdemo.DTO.Result;
import com.hebei.systemdemo.service.IProductionLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/productionLine")
public class ProductionLineController {
    @Autowired
    private IProductionLineService productionLineService;

    @GetMapping("/getProductionLineList")
    public Result getProductionLineList() {
        return productionLineService.getProductionLineList();
    }
}
