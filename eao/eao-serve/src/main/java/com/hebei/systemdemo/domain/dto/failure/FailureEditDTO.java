package com.hebei.systemdemo.domain.dto.failure;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FailureEditDTO {
    @NotNull(message = "ID不能为空")
    private Long id;

    @Length(max = 64, message = "故障编号长度不能超过64")
    private String failureCode;

    @Length(max = 64, message = "设备部位编码长度不能超过64")
    private String devicePartCode;

    @Length(max = 100, message = "设备部位名称长度不能超过100")
    private String devicePartName;

    @Length(max = 100, message = "故障名称长度不能超过100")
    private String failureName;

    @Length(max = 500, message = "故障现象长度不能超过500")
    private String failurePhenomenon;

    @Length(max = 1000, message = "故障描述长度不能超过1000")
    private String failureDescription;

    @Length(max = 100, message = "故障事故通知单类型长度不能超过100")
    private String failureNoticeType;

    @Length(max = 10, message = "故障分类长度不能超过10")
    private String failureType;

    @Length(max = 32, message = "故障开始时间长度不能超过32")
    private String failureStartTime;

    @Length(max = 10, message = "状态长度不能超过10")
    private String status;
}
