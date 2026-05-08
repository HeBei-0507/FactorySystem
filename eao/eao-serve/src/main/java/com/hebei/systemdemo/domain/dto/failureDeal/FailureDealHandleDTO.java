package com.hebei.systemdemo.domain.dto.failureDeal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FailureDealHandleDTO {
    @NotNull(message = "故障记录ID不能为空")
    private Long failureId;

    @Length(max = 32, message = "事件等级长度不能超过32")
    private String severityLevel;

    @Length(max = 32, message = "处理时间长度不能超过32")
    private String processTime;

    @Length(max = 1000, message = "处理说明长度不能超过1000")
    private String processDescription;
}
