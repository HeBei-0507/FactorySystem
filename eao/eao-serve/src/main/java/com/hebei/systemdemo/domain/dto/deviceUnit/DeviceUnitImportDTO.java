package com.hebei.systemdemo.domain.dto.deviceUnit;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DeviceUnitImportDTO {
    private List<DeviceUnitAddDTO> deviceUnitAddDTOList;
}
