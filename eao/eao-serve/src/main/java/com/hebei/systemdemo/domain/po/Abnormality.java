package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Abnormality {
    private Long id;
    private String abnormalCode;
    private String deviceUnitCode;
    private String partName;
    private String source;
    private String abnormalLocation;
    private String abnormalType;
    private String safetyOutput;
    private String reporter;
    private String abnormalDescription;
    private String reportDate;
    private String status;
    private String handler;
    private String handlerName;
    private String handlerDate;
    private String reviewDate;
    private String abnormalOccurredDate;
    private String processMethod;
    private String processOpinion;
    private String creatorUsername;
    private String creatorName;
    private String createdAt;
    private String updatedAt;
}
