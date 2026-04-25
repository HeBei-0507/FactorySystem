package com.hebei.systemdemo.pojo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Equipment {
    private Long id;
    @NotBlank
    private String unitCode;
    @NotBlank
    private String unitName;
    @NotBlank
    private String equipmentCode;
    @NotBlank
    private String equipmentName;
    private String maintainerName;
    private String equipmentCategory;
    private String equipmentImportance;
    private String repairStrategy;
    private String overhaulTeam;
    private String operateTeam;
    private String createdAt;
    private String updatedAt;
}
