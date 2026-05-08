package com.hebei.systemdemo.domain.dto.dailyInspectionFrequency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DailyInspectionFrequencyAddDTO {
    @NotNull(message = "所属日常巡检表ID不能为空")
    private Long dailyInspectionTableId;

    @NotBlank(message = "日常巡检频次编号不能为空")
    @Length(max = 50, message = "日常巡检频次编号长度不能超过50")
    private String frequencyCode;
    
    @NotBlank(message = "巡检区域设备/部位名称不能为空")
    @Length(max = 100, message = "巡检区域设备/部位名称长度不能超过100")
    private String areaDeviceName;
    
    @Length(max = 100, message = "巡检内容长度不能超过100")
    private String inspectionContent;
    
    @Length(max = 100, message = "巡检判定标准长度不能超过100")
    private String inspectionStandard;
    
    @Length(max = 50, message = "数据类别长度不能超过50")
    private String dataType;
    
    @Length(max = 50, message = "巡检周期单位长度不能超过50")
    private String cycleUnit;
    
    private Integer frequencyValue;
    
    private String monthDateSet;
    
    @Length(max = 50, message = "周日期设定长度不能超过50")
    private String weekDateSet;
    
    @Length(max = 50, message = "班次设定长度不能超过50")
    private String shiftsSet;
    
    private BigDecimal maximums;
    
    private BigDecimal minimums;
}
