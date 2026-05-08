package com.hebei.systemdemo.domain.dto.equipmentPart;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EquipmentPartEditDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
    private Long equipmentId;

    @Length(max = 100, message = "设备编码长度不能超过100")
    private String deviceCode;

    @Length(max = 100, message = "设备名称长度不能超过100")
    private String deviceName;

    @Length(max = 100, message = "设备部位编码长度不能超过100")
    private String partCode;

    @Length(max = 100, message = "设备部位名称长度不能超过100")
    private String partName;

    @Length(max = 50, message = "检修班组长度不能超过50")
    private String repairTeam;

    @Length(max = 50, message = "操作班组长度不能超过50")
    private String operateTeam;

    @Length(max = 50, message = "维修策略长度不能超过50")
    private String repairStrategy;

    @Length(max = 50, message = "重要度长度不能超过50")
    private String importanceLevel;

    @Length(max = 50, message = "部位类别长度不能超过50")
    private String partType;

    @Length(max = 64, message = "维护人长度不能超过64")
    private String maintainer;
}
