package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaintenancePlan {
    private Long id;
    private String planCode;
    private String productionLineCode;
    private String productionLineName;
    private String maintenanceCategory;
    private String maintenanceStartTime;
    private String maintenanceEndTime;
    private String status;
    private Long creatorId;
    private String creatorUsername;
    private String creatorName;
    private String createdAt;
    private String updatedAt;
}
