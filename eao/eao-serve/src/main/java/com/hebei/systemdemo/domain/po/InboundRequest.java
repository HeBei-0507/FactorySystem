package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InboundRequest {
    private Long id;
    private Long productionLineId;
    private String productionLineName;
    private String inboundNo;
    private Long materialCodeId;
    private String materialCode;
    private String materialName;
    private String modelSpecification;
    private Long warehouseLocationId;
    private String areaCode;
    private String locationCode;
    private Integer inboundQty;
    private Double unitPrice;
    private Double inboundAmount;
    private String inboundStatus;
    private String inventoryProperty;
    private String inboundType;
    private String inboundDate;
    private Long creatorId;
    private String creatorUsername;
    private String creatorName;
    private String createdAt;
    private String updatedAt;
}
