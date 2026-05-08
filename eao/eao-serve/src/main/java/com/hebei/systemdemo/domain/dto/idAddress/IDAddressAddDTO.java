package com.hebei.systemdemo.domain.dto.idAddress;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class IDAddressAddDTO {
    @NotBlank(message = "ID位置编码不能为空")
    @Length(max = 64, message = "ID位置编码长度不能超过64")
    private String idLocationCode;

    @NotBlank(message = "ID位置不能为空")
    @Length(max = 128, message = "ID位置长度不能超过128")
    private String idLocationName;

    @Length(max = 64, message = "ID位置内码长度不能超过64")
    private String idLocationInnerCode;

    @Length(max = 32, message = "位置类型长度不能超过32")
    private String locationType;

    @Length(max = 64, message = "ID位置分类长度不能超过64")
    private String idLocationCategory;

    @Length(max = 64, message = "创建人长度不能超过64")
    private String creatorUsername;

    @Length(max = 64, message = "创建人姓名长度不能超过64")
    private String creatorName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate createdAt;
}
