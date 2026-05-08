package com.hebei.systemdemo.domain.po;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EquipmentPart {
    private Long id;
    private Long equipmentId;

    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    private String deviceName;

    @NotBlank(message = "设备部位编码不能为空")
    private String partCode;

    @NotBlank(message = "设备部位名称不能为空")
    private String partName;

    private String repairTeam;
    private String operateTeam;
    private String repairStrategy;
    private String importanceLevel;
    private String partType;
    private String maintainer;
    private Long creatorId;
    private String createdAt;
    private String updatedAt;
}
