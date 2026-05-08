package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FailureDeal {
    private Long id;
    private Long failureId;
    private String severityLevel;
    private String processTime;
    private String processDescription;
    private String createdAt;
    private String updatedAt;
}
