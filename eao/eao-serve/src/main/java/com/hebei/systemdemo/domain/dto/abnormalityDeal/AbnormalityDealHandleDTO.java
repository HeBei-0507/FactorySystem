package com.hebei.systemdemo.domain.dto.abnormalityDeal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AbnormalityDealHandleDTO {
    @NotNull(message = "异常记录ID不能为空")
    private Long abnormalityId;

    @Length(max = 64, message = "处理人长度不能超过64")
    private String processor;

    @Length(max = 10, message = "处理方式长度不能超过10")
    private String processMethod;

    @Length(max = 32, message = "处理日期长度不能超过32")
    private String processDate;

    @Length(max = 32, message = "复检日期长度不能超过32")
    private String reviewDate;

    @Length(max = 32, message = "异常发生日期长度不能超过32")
    private String abnormalOccurredDate;

    @Length(max = 500, message = "处理/受审意见长度不能超过500")
    private String processOpinion;
}
