package com.hebei.systemdemo.domain.dto.abnormality;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AbnormalityAddDTO {
    @NotBlank(message = "异常单编号不能为空")
    @Length(max = 64, message = "异常单编号长度不能超过64")
    private String abnormalCode;

    @Length(max = 64, message = "设备部位编码长度不能超过64")
    private String deviceUnitCode;

    @Length(max = 64, message = "来源长度不能超过64")
    private String source;

    @Length(max = 100, message = "异常位置长度不能超过100")
    private String abnormalLocation;

    @Length(max = 10, message = "异常类型长度不能超过10")
    private String abnormalType;

    @Length(max = 10, message = "安全输出长度不能超过10")
    private String safetyOutput;

    @Length(max = 64, message = "报出人长度不能超过64")
    private String reporter;

    @Length(max = 500, message = "异常现象长度不能超过500")
    private String abnormalDescription;

    @Length(max = 32, message = "报出日期长度不能超过32")
    private String reportDate;

    @Length(max = 10, message = "状态长度不能超过10")
    private String status;

    @Length(max = 64, message = "处理人长度不能超过64")
    private String handler;

    @Length(max = 64, message = "处理人姓名长度不能超过64")
    private String handlerName;

    @Length(max = 32, message = "处理日期长度不能超过32")
    private String handlerDate;
}
