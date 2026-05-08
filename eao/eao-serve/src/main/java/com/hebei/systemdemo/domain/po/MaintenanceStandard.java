package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaintenanceStandard {
    private Long id;
    private String standardCode;
    private String partCode;
    private String partName;
    private String itemName;
    private String riskFactor;
    private String safetyMeasure;
    private String workCategory;
    private String workContent;
    private String acceptanceLevel;
    private String maintenanceCategory;
    private String profession;
    private Integer cycleValue;
    private String cycleUnit;
    private String lastCompletionDate;
    private String nextScheduleDate;
    private Long maintainerId;
    private String maintainerName;
    private String maintainerUsername;
    private String maintenanceStartTime;
    private String maintenanceEndTime;
    private String createdAt;
    private String updatedAt;
}
