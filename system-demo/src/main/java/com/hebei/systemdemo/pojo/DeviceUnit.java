package com.hebei.systemdemo.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceUnit {
    private Long id;
    @NotNull
    private Long productionLineId;
    private String productionLineName;
    @NotBlank
    private String unitCode;
    @NotBlank
    private String unitName;
    @NotNull
    private Long creatorId;
    private String creatorName;
    private String createAt;
    private String updateAt;
}
