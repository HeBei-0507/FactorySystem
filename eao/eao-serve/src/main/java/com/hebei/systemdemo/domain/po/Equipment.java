package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Equipment {
    private Long id;
    private Long deviceUnitId;
    private String unitCode;
    private String unitName;
    private String equipmentCode;
    private String equipmentName;
    private String maintainerName;
    private String equipmentCategory;
    private String equipmentImportance;
    private String repairStrategy;
    private String overhaulTeam;
    private String actTeam;
    private Long creatorId;
    private String createdAt;
    private String updatedAt;
}
