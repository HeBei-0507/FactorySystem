package com.hebei.systemdemo.domain.dto.equipment;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EquipmentImportDTO {
    private List<EquipmentAddDTO> equipmentAddDTOList;
}
