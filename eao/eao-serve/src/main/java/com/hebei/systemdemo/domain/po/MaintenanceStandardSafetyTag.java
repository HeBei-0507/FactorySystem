package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaintenanceStandardSafetyTag {
    private Long id;
    private Long standardId;
    private String tagNature;
    private String tagLocation;
    private String tagDeviceCode;
    private String createdAt;
    private String updatedAt;
}
