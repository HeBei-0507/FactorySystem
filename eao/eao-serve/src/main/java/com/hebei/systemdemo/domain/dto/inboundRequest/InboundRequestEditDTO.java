package com.hebei.systemdemo.domain.dto.inboundRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InboundRequestEditDTO {
    @NotNull(message = "入库申请ID不能为空")
    private Long id;
    @NotNull(message = "物料ID不能为空")
    private Long materialCodeId;
    @NotBlank(message = "物料代码不能为空")
    private String materialCode;
    @NotBlank(message = "物料名称不能为空")
    private String materialName;
    private String modelSpecification;
    @NotNull(message = "库区库位ID不能为空")
    private Long warehouseLocationId;
    @NotBlank(message = "库区不能为空")
    private String areaCode;
    @NotBlank(message = "库位不能为空")
    private String locationCode;
    @NotNull(message = "入库数量不能为空")
    private Integer inboundQty;
    @NotNull(message = "单价不能为空")
    private Double unitPrice;
    @NotBlank(message = "库存属性不能为空")
    private String inventoryProperty;
    @NotBlank(message = "入库类型不能为空")
    private String inboundType;
    @NotBlank(message = "入库日期不能为空")
    private String inboundDate;
}
