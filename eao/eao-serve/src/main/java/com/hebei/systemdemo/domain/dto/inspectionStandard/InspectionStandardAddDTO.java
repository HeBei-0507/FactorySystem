package com.hebei.systemdemo.domain.dto.inspectionStandard;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InspectionStandardAddDTO {
    
    @NotBlank(message = "点检标准名称不能为空")
    @Length(max = 100, message = "点检标准名称长度不能超过100")
    private String inspectionName;
    
    @NotBlank(message = "设备部位编码不能为空")
    @Length(max = 100, message = "设备部位编码长度不能超过100")
    private String partCode;
    
    @Length(max = 100, message = "设备部位名称长度不能超过100")
    private String partName;
    
    private Integer implementationCycle;
    
    @Length(max = 100, message = "周期单位长度不能超过100")
    private String cycleUnit;
    
    @Length(max = 100, message = "数据类型长度不能超过100")
    private String dataType;
    
    @Length(max = 100, message = "信号类型长度不能超过100")
    private String signalType;
    
    @Length(max = 100, message = "实施方法长度不能超过100")
    private String implementationMethod;
    
    @Length(max = 255, message = "点检内容长度不能超过255")
    private String inspectionContent;
    
    @Length(max = 100, message = "点检部位长度不能超过100")
    private String inspectionPart;
    
    @Length(max = 255, message = "定性标准长度不能超过255")
    private String qualitativeStandard;
    
    @Length(max = 100, message = "标准类别长度不能超过100")
    private String standardCategory;
    
    @Length(max = 255, message = "定量标准长度不能超过255")
    private String quantitativeStandard;
    
    @Length(max = 100, message = "计量单位长度不能超过100")
    private String unitOfMeasurement;
    
    @Length(max = 50, message = "专业长度不能超过50")
    private String profession;
    
    @Length(max = 255, message = "上限长度不能超过255")
    private String upperLimit;
    
    @Length(max = 255, message = "下限长度不能超过255")
    private String lowerLimit;

    @Length(max = 64, message = "ID位置编码长度不能超过64")
    private String idLocationCode;

    @Length(max = 128, message = "ID位置长度不能超过128")
    private String idLocationName;
}
