package com.hebei.systemdemo.domain.dto.maintenanceTicket;

import jakarta.validation.constraints.NotBlank;
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
public class MaintenanceTicketAddDTO {
    @NotBlank(message = "工单名称不能为空")
    @Length(max = 100, message = "工单名称长度不能超过100")
    private String ticketName;

    @Length(max = 100, message = "状态长度不能超过100")
    private String status;
    @Length(max = 100, message = "检修保养计划长度不能超过100")
    private String maintenancePlanName;
    @Length(max = 100, message = "操作班组长度不能超过100")
    private String operationTeam;
    @Length(max = 100, message = "任务类型长度不能超过100")
    private String taskType;
    @Length(max = 100, message = "是否影响生产长度不能超过100")
    private String productionImpact;
    @Length(max = 100, message = "委托类型长度不能超过100")
    private String entrustType;
    @Length(max = 255, message = "检修资源长度不能超过255")
    private String maintenanceResource;
    @Length(max = 500, message = "维修内容长度不能超过500")
    private String maintenanceContent;
    @Length(max = 500, message = "异常理象长度不能超过500")
    private String abnormalPhenomenon;
    @Length(max = 500, message = "处理受理意见长度不能超过500")
    private String handlingOpinion;
    private Long planId;
    @Length(max = 100, message = "计划编号长度不能超过100")
    private String planCode;
    @Length(max = 100, message = "检修分类长度不能超过100")
    private String maintenanceCategory;
    @Length(max = 32, message = "检修开始时间长度不能超过32")
    private String maintenanceStartTime;
    @Length(max = 32, message = "检修结束时间长度不能超过32")
    private String maintenanceEndTime;
    private Long standardId;
    @Length(max = 100, message = "标准项目编码长度不能超过100")
    private String standardCode;
    @NotBlank(message = "设备部位编码不能为空")
    @Length(max = 100, message = "设备部位编码长度不能超过100")
    private String partCode;
    @Length(max = 100, message = "设备部位名称长度不能超过100")
    private String partName;
    @NotBlank(message = "项目名称不能为空")
    @Length(max = 100, message = "项目名称长度不能超过100")
    private String itemName;
    @Length(max = 100, message = "专业长度不能超过100")
    private String profession;
    @Length(max = 255, message = "风险因素长度不能超过255")
    private String riskFactor;
    @Length(max = 255, message = "安全防范措施长度不能超过255")
    private String safetyMeasure;
    @Length(max = 100, message = "工事分类长度不能超过100")
    private String workCategory;
    @Length(max = 100, message = "验收等级长度不能超过100")
    private String acceptanceLevel;
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
