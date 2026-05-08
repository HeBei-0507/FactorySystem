package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Failure {
    private Long id;
    private String failureCode;
    private String devicePartCode;
    private String devicePartName;
    private String failureName;
    private String failurePhenomenon;
    private String failureDescription;
    private String failureNoticeType;
    private String failureType;
    private String failureStartTime;
    private String status;
    private String severityLevel;
    private String processTime;
    private String processDescription;
    private String creatorUsername;
    private String creatorName;
    private String createdAt;
    private String updatedAt;
}
