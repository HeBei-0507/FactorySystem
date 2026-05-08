package com.hebei.systemdemo.domain.dto.supplierManage;

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
public class SupplierManageAddDTO {
    @Length(max = 64, message = "供应商分类长度不能超过64")
    private String supplierCategory;

    @NotBlank(message = "供应商代码不能为空")
    @Length(max = 64, message = "供应商代码长度不能超过64")
    private String supplierCode;

    @NotBlank(message = "供应商名称不能为空")
    @Length(max = 128, message = "供应商名称长度不能超过128")
    private String supplierName;

    @Length(max = 255, message = "地址长度不能超过255")
    private String address;

    @Length(max = 64, message = "联系人长度不能超过64")
    private String contactPerson;

    @Length(max = 64, message = "联系人电话长度不能超过64")
    private String contactId;

    @Length(max = 128, message = "联系人邮箱长度不能超过128")
    private String contactEmail;

    @Length(max = 64, message = "传真长度不能超过64")
    private String fax;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate createdAt;
}
