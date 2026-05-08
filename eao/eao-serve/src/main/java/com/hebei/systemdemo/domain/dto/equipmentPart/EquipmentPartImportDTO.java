package com.hebei.systemdemo.domain.dto.equipmentPart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EquipmentPartImportDTO {
    
    @Valid
    @NotEmpty(message = "导入数据不能为空")
    private List<EquipmentPartAddDTO> equipmentPartList;
}
