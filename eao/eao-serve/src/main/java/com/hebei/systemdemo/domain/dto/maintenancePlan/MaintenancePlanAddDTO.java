package com.hebei.systemdemo.domain.dto.maintenancePlan;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaintenancePlanAddDTO {
    @NotBlank(message = "生产线代码不能为空")
    @Length(max = 100, message = "生产线代码长度不能超过100")
    private String productionLineCode;

    @Length(max = 100, message = "生产线名称长度不能超过100")
    private String productionLineName;

    @NotBlank(message = "检修分类不能为空")
    @Length(max = 100, message = "检修分类长度不能超过100")
    private String maintenanceCategory;

    @NotBlank(message = "检修开始时间不能为空")
    @Length(max = 32, message = "检修开始时间长度不能超过32")
    private String maintenanceStartTime;

    @NotBlank(message = "检修结束时间不能为空")
    @Length(max = 32, message = "检修结束时间长度不能超过32")
    private String maintenanceEndTime;

    @Length(max = 100, message = "状态长度不能超过100")
    private String status;
}
