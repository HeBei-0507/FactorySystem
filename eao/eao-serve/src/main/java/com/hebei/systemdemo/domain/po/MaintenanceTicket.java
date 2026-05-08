package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaintenanceTicket {
    private Long id;
    private String ticketCode;
    private String ticketName;
    private String status;
    private String maintenancePlanName;
    private String operationTeam;
    private String taskType;
    private String productionImpact;
    private String entrustType;
    private String maintenanceResource;
    private String maintenanceContent;
    private String abnormalPhenomenon;
    private String handlingOpinion;
    private Long planId;
    private String planCode;
    private String maintenanceCategory;
    private String maintenanceStartTime;
    private String maintenanceEndTime;
    private Long standardId;
    private String standardCode;
    private String partCode;
    private String partName;
    private String itemName;
    private String profession;
    private String riskFactor;
    private String safetyMeasure;
    private String workCategory;
    private String acceptanceLevel;
    private Long creatorId;
    private String creatorUsername;
    private String creatorName;
    private String createdAt;
    private String updatedAt;
}
