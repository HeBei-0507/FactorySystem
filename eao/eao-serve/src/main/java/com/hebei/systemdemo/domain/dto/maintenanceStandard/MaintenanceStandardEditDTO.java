package com.hebei.systemdemo.domain.dto.maintenanceStandard;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaintenanceStandardEditDTO extends MaintenanceStandardAddDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
}
