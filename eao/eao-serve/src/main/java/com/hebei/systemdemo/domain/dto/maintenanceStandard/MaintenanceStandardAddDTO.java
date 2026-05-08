package com.hebei.systemdemo.domain.dto.maintenanceStandard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaintenanceStandardAddDTO {
    @NotBlank(message = "设备部位编码不能为空")
    @Length(max = 100, message = "设备部位编码长度不能超过100")
    private String partCode;

    @Length(max = 100, message = "设备部位名称长度不能超过100")
    private String partName;

    @NotBlank(message = "标准项目名称不能为空")
    @Length(max = 100, message = "标准项目名称长度不能超过100")
    private String itemName;

    @Length(max = 255, message = "风险因素长度不能超过255")
    private String riskFactor;

    @Length(max = 255, message = "安全防范措施长度不能超过255")
    private String safetyMeasure;

    @Length(max = 100, message = "工事分类长度不能超过100")
    private String workCategory;

    @Length(max = 255, message = "工事内容长度不能超过255")
    private String workContent;

    @Length(max = 100, message = "验收等级长度不能超过100")
    private String acceptanceLevel;

    @Length(max = 100, message = "检修分类长度不能超过100")
    private String maintenanceCategory;

    @Length(max = 100, message = "专业长度不能超过100")
    private String profession;

    private Integer cycleValue;

    @Length(max = 100, message = "周期单位长度不能超过100")
    private String cycleUnit;

    @Length(max = 32, message = "上次完工日期长度不能超过32")
    private String lastCompletionDate;

    @Length(max = 32, message = "下次排程日期长度不能超过32")
    private String nextScheduleDate;

    @Length(max = 32, message = "维护开始时间长度不能超过32")
    private String maintenanceStartTime;

    @Length(max = 32, message = "维护结束时间长度不能超过32")
    private String maintenanceEndTime;

    private List<MaterialDetailDTO> spareParts = new ArrayList<>();
    private List<MaterialDetailDTO> tools = new ArrayList<>();
    private List<SafetyTagDetailDTO> safetyTags = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class MaterialDetailDTO {
        private Long id;
        @NotNull(message = "物料ID不能为空")
        private Long materialCodeId;
        @NotNull(message = "入库申请ID不能为空")
        private Long inboundRequestId;
        @NotBlank(message = "物料代码不能为空")
        private String materialCode;
        @NotBlank(message = "物料名称不能为空")
        private String materialName;
        private String materialSubCategory;
        private String modelSpecification;
        @NotNull(message = "数量不能为空")
        private Integer quantity;
        @Length(max = 50, message = "数量单位长度不能超过50")
        private String quantityUnit;
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class SafetyTagDetailDTO {
        private Long id;
        @Length(max = 100, message = "安全牌性质长度不能超过100")
        private String tagNature;
        @Length(max = 100, message = "挂牌位置长度不能超过100")
        private String tagLocation;
        @Length(max = 100, message = "挂牌设备编码长度不能超过100")
        private String tagDeviceCode;
    }
}
