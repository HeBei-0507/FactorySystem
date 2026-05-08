package com.hebei.systemdemo.domain.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class SupplierManage {
    private Long id;
    private String supplierCategory;
    private String supplierCode;
    private String supplierName;
    private String address;
    private String contactPerson;
    private String contactId;
    private String contactEmail;
    private String fax;
    private Long creatorId;
    private String creatorUsername;
    private String creatorName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate createdAt;
}
