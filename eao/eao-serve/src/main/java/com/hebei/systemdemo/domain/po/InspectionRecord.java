package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InspectionRecord {
    private Long id;
    private Long inspectionStandardId;
    private Long productionLineId;
    private String productionLineName;
    private Long routeId;
    private String routeCode;
    private String routeName;
    private String planDate;
    private String currentResult;
    private String abnormalType;
    private String abnormalPhenomenon;
    private String planSource;
    private String standardCode;
    private String partCode;
    private String partName;
    private String inspectionPart;
    private String inspectionContent;
    private String completedAt;
    private Integer implementationCycle;
    private String cycleUnit;
    private String completionFlag;
    private String qualitativeStandard;
    private String standardCategory;
    private String quantitativeStandard;
    private String unitOfMeasurement;
    private String upperLimit;
    private String lowerLimit;
    private String idLocationCode;
    private String idLocationName;
    private String abnormalContactSheet;
    private Long creatorId;
    private String creatorUsername;
    private String creatorName;
    private String createdAt;
    private String updatedAt;
    private Integer totalCount;
    private Integer inspectedCount;
    private Integer remainingCount;
}
