package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaintenanceStandardTool {
    private Long id;
    private Long standardId;
    private Long materialCodeId;
    private Long inboundRequestId;
    private String materialCode;
    private String materialName;
    private String materialSubCategory;
    private String modelSpecification;
    private Integer quantity;
    private String quantityUnit;
    private String createdAt;
    private String updatedAt;
}
