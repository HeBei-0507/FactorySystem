package com.hebei.systemdemo.domain.dto.materialCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaterialCodeEditDTO {
    @NotNull(message = "物料代码ID不能为空")
    private Long id;

    @NotBlank(message = "物料代码不能为空")
    @Length(max = 50, message = "物料代码长度不能超过50")
    private String materialCode;

    @NotBlank(message = "物料名称不能为空")
    @Length(max = 100, message = "物料名称长度不能超过100")
    private String materialName;

    @Length(max = 255, message = "物料描述长度不能超过255")
    private String materialDescription;

    @Length(max = 100, message = "型号规格长度不能超过100")
    private String modelSpecification;

    @Length(max = 100, message = "图样编号长度不能超过100")
    private String drawingNo;

    @Length(max = 100, message = "物料大分类长度不能超过100")
    private String materialMajorCategory;

    @Length(max = 100, message = "物料小分类长度不能超过100")
    private String materialSubCategory;

    @Length(max = 100, message = "物料属性长度不能超过100")
    private String materialProperty;

    @Length(max = 20, message = "状态长度不能超过20")
    private String status;
}
