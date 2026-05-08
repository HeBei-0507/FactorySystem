package com.hebei.systemdemo.domain.dto.lubricationStandard;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LubricationStandardEditDTO {
    
    @NotNull(message = "ID不能为空")
    private Long id;
    
    @Length(max = 100, message = "设备部位编码长度不能超过100")
    private String partCode;
    
    @Length(max = 100, message = "设备部位名称长度不能超过100")
    private String partName;
    
    @Length(max = 100, message = "润滑标准名称长度不能超过100")
    private String standardName;
    
    @Pattern(regexp = "^(01|02|03|04|05|06)$", message = "润滑方式必须是：01-手工润滑、02-滴注润滑、03-飞溅润滑、04-油环与油链润滑、05-油绳与油垫润滑、06-自润滑")
    private String method;
    
    @Pattern(regexp = "^(01|02|03|04)$", message = "专业必须是：01-机械专业、02-电气专业、03-仪表专业、04-过程控制专业")
    private String profession;
    
    @Pattern(regexp = "^(01|02|03)$", message = "给油类型必须是：01-补漏、02-换油、03-油质化验")
    private String oilFeedType;
    
    @Length(max = 100, message = "加油点长度不能超过100")
    private String feedPoint;
    
    @Length(max = 100, message = "油质型号长度不能超过100")
    private String oilModels;
    
    @Length(max = 100, message = "油量长度不能超过100")
    private String oilVolume;
    
    private String nextCheckTime;
}
