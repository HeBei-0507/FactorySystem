package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DailyInspectionFrequency {
    private Long id;
    private Long dailyInspectionTableId;
    private String frequencyCode;
    private String areaDeviceName;
    private String inspectionContent;
    private String inspectionStandard;
    private String dataType;
    private String cycleUnit;
    private Integer frequencyValue;
    private String monthDateSet;
    private String weekDateSet;
    private String shiftsSet;
    private BigDecimal maximums;
    private BigDecimal minimums;
    private Long creatorId;
    private String creatorName;
    private String creatorUsername;
    private String createdAt;
    private String updatedAt;
}
