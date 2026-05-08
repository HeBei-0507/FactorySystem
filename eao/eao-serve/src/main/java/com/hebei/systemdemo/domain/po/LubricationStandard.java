package com.hebei.systemdemo.domain.po;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


// 润滑标准

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LubricationStandard {
    private Long id;
    
    @NotBlank(message = "设备部位编码不能为空")
    private String partCode;
    
    private String partName;
    
    @NotBlank(message = "润滑标准编码不能为空")
    private String standardCode;
    
    @NotBlank(message = "润滑标准名称不能为空")
    private String standardName;
    
    @NotBlank(message = "润滑方式不能为空")
    private String method;
    
    @NotBlank(message = "专业不能为空")
    private String profession;
    
    @NotBlank(message = "给油类型不能为空")
    private String oilFeedType;
    
    @NotBlank(message = "加油点不能为空")
    private String feedPoint;
    
    @NotBlank(message = "油质型号不能为空")
    private String oilModels;
    
    @NotBlank(message = "油量不能为空")
    private String oilVolume;
    
    @NotNull(message = "下次润滑日期不能为空")
    private String nextCheckTime;
    
    private Long creatorId;
    private String createdAt;
    private String updatedAt;
}
