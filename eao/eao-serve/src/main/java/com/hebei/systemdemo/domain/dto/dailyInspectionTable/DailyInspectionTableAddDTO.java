package com.hebei.systemdemo.domain.dto.dailyInspectionTable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DailyInspectionTableAddDTO {
    @Length(max = 50, message = "日常巡检表编号长度不能超过50")
    private String tableCode;
    
    @NotBlank(message = "日常巡检表名称不能为空")
    @Length(max = 100, message = "日常巡检表名称长度不能超过100")
    private String tableName;
    
    @NotNull(message = "作业线ID不能为空")
    private Long productionLineId;
    
    @Length(max = 100, message = "倒班模式长度不能超过100")
    private String shiftMode;
    
    @Length(max = 100, message = "实施人长度不能超过100")
    private String implementer;
    
    @Length(max = 100, message = "状态长度不能超过100")
    private String status;
}
