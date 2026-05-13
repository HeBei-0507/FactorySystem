package com.hebei.systemdemo.domain.dto.inspectionRecord;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class InspectionRecordEditDTO {
    @NotNull(message = "点检实绩ID不能为空")
    private Long id;

    private Long productionLineId;
    private Long routeId;

    @Length(max = 100, message = "路线编号长度不能超过100")
    private String routeCode;

    @Length(max = 100, message = "路线名称长度不能超过100")
    private String routeName;

    @Length(max = 32, message = "计划日期长度不能超过32")
    private String planDate;

    @Length(max = 10, message = "当前结果长度不能超过10")
    private String currentResult;

    @Length(max = 10, message = "异常类型长度不能超过10")
    private String abnormalType;

    @Length(max = 500, message = "异常现象长度不能超过500")
    private String abnormalPhenomenon;

    @Length(max = 32, message = "计划来源长度不能超过32")
    private String planSource;

    @Length(max = 100, message = "标准编号长度不能超过100")
    private String standardCode;

    @Length(max = 100, message = "设备部位编码长度不能超过100")
    private String partCode;

    @Length(max = 100, message = "设备部位名称长度不能超过100")
    private String partName;

    @Length(max = 100, message = "点检部位长度不能超过100")
    private String inspectionPart;

    @Length(max = 255, message = "点检内容长度不能超过255")
    private String inspectionContent;

    @Length(max = 32, message = "完工日期长度不能超过32")
    private String completedAt;

    private Integer implementationCycle;

    @Length(max = 100, message = "周期单位长度不能超过100")
    private String cycleUnit;

    @Length(max = 10, message = "完工标记长度不能超过10")
    private String completionFlag;

    @Length(max = 255, message = "定性标准长度不能超过255")
    private String qualitativeStandard;

    @Length(max = 100, message = "标准类别长度不能超过100")
    private String standardCategory;

    @Length(max = 255, message = "定量标准长度不能超过255")
    private String quantitativeStandard;

    @Length(max = 100, message = "计量单位长度不能超过100")
    private String unitOfMeasurement;

    @Length(max = 255, message = "上限长度不能超过255")
    private String upperLimit;

    @Length(max = 255, message = "下限长度不能超过255")
    private String lowerLimit;

    @Length(max = 64, message = "ID位置编码长度不能超过64")
    private String idLocationCode;

    @Length(max = 128, message = "ID位置长度不能超过128")
    private String idLocationName;

    @Length(max = 100, message = "异常联络单长度不能超过100")
    private String abnormalContactSheet;
}