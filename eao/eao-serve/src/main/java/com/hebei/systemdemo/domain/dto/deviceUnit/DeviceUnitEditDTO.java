package com.hebei.systemdemo.domain.dto.deviceUnit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DeviceUnitEditDTO {
    @NotNull(message = "设备单元ID不能为空")
    private Long id;
    @NotBlank(message = "设备单元编码不能为空")
    @Length(max = 50, message = "设备单元编码长度不能超过50")
    private String unitCode;
    @NotBlank(message = "设备单元名称不能为空")
    @Length(max = 50, message = "设备单元名称长度不能超过50")
    private String unitName;
}
