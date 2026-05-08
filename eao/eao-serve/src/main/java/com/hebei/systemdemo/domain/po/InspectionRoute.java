package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InspectionRoute {
    private Long id;
    private Long productionLineId;
    private String productionLineName;
    private String routeCode;
    private String routeName;
    private Long creatorId;
    private String creatorName;
    private String creatorUsername;
    private String createdAt;
    private String updatedAt;
}
