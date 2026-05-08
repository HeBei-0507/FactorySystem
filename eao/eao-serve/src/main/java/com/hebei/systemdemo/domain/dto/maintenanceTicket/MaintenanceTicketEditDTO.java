package com.hebei.systemdemo.domain.dto.maintenanceTicket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaintenanceTicketEditDTO extends MaintenanceTicketAddDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
}
