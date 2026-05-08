package com.hebei.systemdemo.domain.po;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

// 检测路线设备

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InspectionRouteDevice {
    private Long id;
    private String routeCode;
    private String routeName;
    private String equipmentCode;
    private String equipmentName;
    private Integer sort;
    private Long creatorId;
    private String creatorName;
    private String creatorUsername;
    private String createdAt;
    private String updatedAt;
}
