package com.hebei.systemdemo.domain.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AbnormalityDeal {
    private Long id;
    private Long abnormalityId;
    private String processor;
    private String processMethod;
    private String processDate;
    private String reviewDate;
    private String abnormalOccurredDate;
    private String processOpinion;
    private String createdAt;
    private String updatedAt;
}
