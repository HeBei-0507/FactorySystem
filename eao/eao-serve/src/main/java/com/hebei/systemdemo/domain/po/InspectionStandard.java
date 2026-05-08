package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

// 检查标准

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InspectionStandard {
    private Long id;
    private String inspectionName;
    private String partCode;
    private String partName;
    private Integer implementationCycle;
    private String cycleUnit;
    private String dataType;
    private String signalType;
    private String implementationMethod;
    private String inspectionContent;
    private String inspectionPart;
    private String qualitativeStandard;
    private String standardCategory;
    private String quantitativeStandard;
    private String unitOfMeasurement;
    private String profession;
    private String upperLimit;
    private String lowerLimit;
    private String idLocationCode;
    private String idLocationName;
    private Long creatorId;
    private String creatorName;
    private String creatorUsername;
    private String createdAt;
    private String updatedAt;
}
