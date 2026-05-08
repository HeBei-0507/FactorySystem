package com.hebei.systemdemo.domain.dto.equipment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EquipmentAddDTO {
    private Long deviceUnitId;

    @NotBlank(message = "设备单元编码不能为空")
    @Length(max = 50,message = " 设备单元编码长度不能超过50")
    private String unitCode;

    @NotBlank(message = "设备单元名称不能为空")
    @Length(max = 50,message = " 设备单元名称长度不能超过50")
    private String unitName;

    private String equipmentCode;
    private String equipmentName;
    private String maintainerName;
    private String equipmentCategory;
    private String equipmentImportance;
    private String repairStrategy;
    private String overhaulTeam;
    private String actTeam;
}
