package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DailyInspectionTable {
    private Long id;
    private String tableCode;
    private String tableName;
    private Long productionLineId;
    private String productionLineName;
    private String shiftMode;
    private String implementer;
    private String status;
    private Long creatorId;
    private String creatorName;
    private String creatorUsername;
    private String createdAt;
    private String updatedAt;
}
