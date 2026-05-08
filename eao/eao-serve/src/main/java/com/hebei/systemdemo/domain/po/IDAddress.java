package com.hebei.systemdemo.domain.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class IDAddress {
    private Long id;
    private String idLocationCode;
    private String idLocationName;
    private String idLocationInnerCode;
    private String locationType;
    private String idLocationCategory;
    private String creatorUsername;
    private String creatorName;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate createdAt;
}

/*
*
* create table id_location
(
    id                     bigint auto_increment comment '主键ID'
        primary key,
    id_location_code       varchar(64)  not null comment 'ID位置编码',
    id_location_name       varchar(128) not null comment 'ID位置',
    id_location_inner_code varchar(64)  null comment 'ID位置内码',
    location_type          varchar(32)  null comment '位置类型',
    id_location_category   varchar(64)  null comment 'ID位置分类',
    creator_username       varchar(64)  null comment '创建人',
    creator_name           varchar(64)  null comment '创建人姓名',
    created_at             date         null comment '创建日期',
    constraint uk_id_location_code
        unique (id_location_code)
)
    comment 'ID位置管理表' collate = utf8mb4_general_ci;
**/