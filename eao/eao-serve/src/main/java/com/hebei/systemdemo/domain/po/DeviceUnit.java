package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DeviceUnit {
    private Long id;
    private Long productionLineId;
    private String productionLineName;
    private String unitCode;
    private String unitName;
    private Long creatorId;
    private String creatorName;
    private String creatorUsername;
    private String createAt;
    private String updateAt;
}
