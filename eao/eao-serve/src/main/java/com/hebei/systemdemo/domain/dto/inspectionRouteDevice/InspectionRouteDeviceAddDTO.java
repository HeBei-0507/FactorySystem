package com.hebei.systemdemo.domain.dto.inspectionRouteDevice;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InspectionRouteDeviceAddDTO {
    @NotBlank(message = "路线编码不能为空")
    @Length(max = 100, message = "路线编码长度不能超过100")
    private String routeCode;
    
    @NotBlank(message = "路线名称不能为空")
    @Length(max = 100, message = "路线名称长度不能超过100")
    private String routeName;
    
    @NotBlank(message = "设备编码不能为空")
    @Length(max = 50, message = "设备编码长度不能超过50")
    private String equipmentCode;
    
    @NotBlank(message = "设备名称不能为空")
    @Length(max = 100, message = "设备名称长度不能超过100")
    private String equipmentName;
    
    private Integer sort;
}
