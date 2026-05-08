package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaintenanceTicketSafetyTag {
    private Long id;
    private Long ticketId;
    private String tagNature;
    private String tagLocation;
    private String tagDeviceCode;
    private String createdAt;
    private String updatedAt;
}
