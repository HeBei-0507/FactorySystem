package com.hebei.systemdemo.domain.dto.inspectionRoute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InspectionRouteAddDTO {
    @NotNull(message = "生产线ID不能为空")
    private Long productionLineId;
    
    @NotBlank(message = "路线编码不能为空")
    @Length(max = 100, message = "路线编码长度不能超过100")
    private String routeCode;
    
    @NotBlank(message = "路线名称不能为空")
    @Length(max = 100, message = "路线名称长度不能超过100")
    private String routeName;
}
