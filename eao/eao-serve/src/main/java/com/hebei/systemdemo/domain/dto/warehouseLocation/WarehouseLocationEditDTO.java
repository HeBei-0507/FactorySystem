package com.hebei.systemdemo.domain.dto.warehouseLocation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class WarehouseLocationEditDTO {
    @NotNull(message = "库区库位ID不能为空")
    private Long id;

    @NotBlank(message = "库区不能为空")
    @Length(max = 50, message = "库区长度不能超过50")
    private String areaCode;

    @NotBlank(message = "库位不能为空")
    @Length(max = 50, message = "库位长度不能超过50")
    private String locationCode;

    @NotBlank(message = "库管员不能为空")
    @Length(max = 50, message = "库管员长度不能超过50")
    private String keeperUsername;

    @NotBlank(message = "库管员姓名不能为空")
    @Length(max = 50, message = "库管员姓名长度不能超过50")
    private String keeperRealName;
}
