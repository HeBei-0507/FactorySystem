package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseLocation {
    private Long id;
    private Long productionLineId;
    private String productionLineName;
    private String areaCode;
    private String locationCode;
    private String keeperUsername;
    private String keeperRealName;
    private Long creatorId;
    private String creatorUsername;
    private String creatorName;
    private String createdAt;
    private String updatedAt;
}
