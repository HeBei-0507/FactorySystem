package com.hebei.systemdemo.domain.dto.maintenancePlan;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class MaintenancePlanEditDTO extends MaintenancePlanAddDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
}
