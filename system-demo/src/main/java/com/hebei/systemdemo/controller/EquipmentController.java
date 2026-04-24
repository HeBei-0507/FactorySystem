package com.hebei.systemdemo.controller;

import com.hebei.systemdemo.DTO.Result;
import com.hebei.systemdemo.pojo.Equipment;
import com.hebei.systemdemo.service.IEquipmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/equipment")
public class EquipmentController {
    @Autowired
    private IEquipmentService equipmentService;

    @GetMapping("/page")
    public Result page(@RequestParam Integer current,
                       @RequestParam Integer size,
                       @RequestParam(required = false) String unitCode,
                       @RequestParam(required = false) String equipmentCode,
                       @RequestParam(required = false) String equipmentName) {
        return equipmentService.page(current, size, unitCode, equipmentCode, equipmentName);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return equipmentService.getById(id);
    }

    @PostMapping("/add")
    public Result add(@Valid @RequestBody Equipment equipment) {
        return equipmentService.add(equipment);
    }

    @PutMapping("/update")
    public Result update(@RequestBody Equipment equipment) {
        return equipmentService.update(equipment);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return equipmentService.deleteById(id);
    }
}
