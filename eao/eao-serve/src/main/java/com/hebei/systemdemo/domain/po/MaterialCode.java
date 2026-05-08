package com.hebei.systemdemo.domain.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MaterialCode {
    private Long id;
    private String materialCode;
    private String materialName;
    private String materialDescription;
    private String modelSpecification;
    private String drawingNo;
    private String materialMajorCategory;
    private String materialSubCategory;
    private String materialProperty;
    private String status;
    private Long creatorId;
    private String creatorName;
    private String creatorUsername;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    private Long modifierId;
    private String modifierName;
    private String modifierUsername;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime modifiedAt;
}

/*
* create table material_code
(
    id                      bigint auto_increment comment '主键ID'
        primary key,
    material_code           varchar(100)                          not null comment '物料代码',
    material_name           varchar(200)                          not null comment '物料名称',
    material_description    varchar(100)                          null comment '物料描述',
    model_specification     varchar(200)                          null comment '型号规格',
    drawing_no              varchar(100)                          null comment '图号',
    material_major_category varchar(100)                          null comment '物料大类',
    material_sub_category   varchar(100)                          null comment '物料分类',
    material_property       varchar(100)                          null comment '物料属性',
    status                  varchar(20) default '1'               null comment '状态：1启用，0停用',
    creator_id              bigint                                null comment '创建人',
    creator_name            varchar(100)                          null comment '创建人姓名',
    created_at              datetime    default CURRENT_TIMESTAMP null comment '创建日期',
    modifier_id             bigint                                null comment '修订人',
    modifier_name           varchar(100)                          null comment '修订人姓名',
    modified_at             datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修订日期',
    constraint uk_material_code
        unique (material_code)
)
    comment '物料代码管理表' collate = utf8mb4_general_ci;

create index idx_material_category
    on material_code (material_major_category, material_sub_category);

create index idx_material_name
    on material_code (material_name);

create index idx_status
    on material_code (status);

*
* */