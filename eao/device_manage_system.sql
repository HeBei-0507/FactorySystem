/*
 Navicat Premium Dump SQL

 Source Server         : 本地MySQL8
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:13306
 Source Schema         : device_manage_system

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 28/04/2026 12:17:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
create table sys_user
(
    id            bigint unsigned auto_increment comment '用户主键'
        primary key,
    username      varchar(50)                        not null comment '登录账号',
    real_name     varchar(50)                        not null comment '真实姓名',
    password_hash varchar(255)                       not null comment '密码哈希',
    phone         varchar(20)                        null comment '手机号',
    status        tinyint  default 1                 not null comment '状态：1启用 0禁用',
    is_deleted    tinyint  default 0                 not null comment '是否删除：0否 1是',
    created_at    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_sys_user_phone
        unique (phone),
    constraint uk_sys_user_username
        unique (username)
)
    comment '系统用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin1', '测试管理员1', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', 1, 0, '2026-04-24 01:37:47', '2026-04-24 01:37:47');
INSERT INTO `sys_user` VALUES (2, 'admin2', '测试管理员2', 'e10adc3949ba59abbe56e057f20f883e', '13800138001', 1, 0, '2026-04-24 01:37:47', '2026-04-24 01:37:47');
INSERT INTO `sys_user` VALUES (3, 'admin3', '测试管理员3', 'e10adc3949ba59abbe56e057f20f883e', '13800138010', 1, 0, '2026-04-24 01:37:47', '2026-04-24 01:37:47');

-- ----------------------------
-- Table structure for production_line
-- ----------------------------
DROP TABLE IF EXISTS `production_line`;
create table production_line
(
    id          bigint unsigned auto_increment comment '生产线主键'
        primary key,
    line_code   varchar(50)                        not null comment '生产线编码',
    line_name   varchar(100)                       not null comment '生产线名称',
    description varchar(500)                       null comment '生产线描述',
    creator_id  bigint unsigned                    null comment '创建人ID',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_production_line_code
        unique (line_code),
    constraint fk_production_line_creator
        foreign key (creator_id) references sys_user (id)
            on update cascade on delete set null
)
    comment '生产线表';

create index idx_production_line_creator_id
    on production_line (creator_id);
-- ----------------------------
-- Records of production_line
-- ----------------------------
INSERT INTO `production_line` VALUES (1, 'RC-AUTO-001', '遥控器自动化生产线', '用于遥控器产品的自动化组装与测试生产线', 1, '2026-04-24 01:37:47', '2026-04-24 01:37:47');

-- ----------------------------
-- Table structure for device_unit
-- ----------------------------
DROP TABLE IF EXISTS `device_unit`;
create table device_unit
(
    id                 bigint unsigned auto_increment comment '设备单元主键'
        primary key,
    production_line_id bigint unsigned                    not null comment '所属生产线ID',
    unit_code          varchar(50)                        not null comment '设备单元代码',
    unit_name          varchar(100)                       not null comment '设备单元名称',
    creator_id         bigint unsigned                    null comment '创建人ID',
    created_at         datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at         datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_device_unit_creator_code
        unique (creator_id, unit_code),
    constraint fk_device_unit_creator
        foreign key (creator_id) references sys_user (id)
            on update cascade on delete set null,
    constraint fk_device_unit_line
        foreign key (production_line_id) references production_line (id)
            on update cascade on delete cascade
)
    comment '设备单元表';

create index idx_device_unit_creator_id
    on device_unit (creator_id);

create index idx_device_unit_line_id
    on device_unit (production_line_id);

-- ----------------------------
-- Table structure for equipment
-- ----------------------------
DROP TABLE IF EXISTS `equipment`;
create table equipment
(
    id                   bigint unsigned auto_increment comment '主键ID'
        primary key,
    unit_code            varchar(50)                        not null comment '设备单元编码',
    unit_name            varchar(100)                       not null comment '设备单元名称',
    device_unit_id       bigint unsigned                    null comment '设备单元ID',
    equipment_code       varchar(50)                        not null comment '设备编码',
    equipment_name       varchar(100)                       not null comment '设备名称',
    maintainer_name      varchar(100)                       null comment '设备维护人',
    equipment_category   varchar(100)                       null comment '设备类别',
    equipment_importance varchar(100)                       null comment '设备重要度',
    repair_strategy      varchar(100)                       null comment '维修策略',
    overhaul_team        varchar(100)                       null comment '检修班组',
    created_at           datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    act_team             varchar(100)                       null comment '操作班组',
    creator_id           bigint unsigned                    null comment '创建人ID',
    constraint uk_equipment_creator_code
        unique (creator_id, equipment_code),
    constraint fk_equipment_device_unit_id
        foreign key (device_unit_id) references device_unit (id)
            on update cascade on delete cascade
)
    comment '设备表';

create index idx_equipment_creator_id
    on equipment (creator_id);

create index idx_equipment_device_unit_id
    on equipment (device_unit_id);


-- ----------------------------
-- Table structure for equipment_part
-- ----------------------------
DROP TABLE IF EXISTS `equipment_part`;
create table equipment_part
(
    id               bigint auto_increment comment '主键ID'
        primary key,
    device_code      varchar(100)                       not null comment '设备编码',
    device_name      varchar(100)                       null comment '设备名称',
    equipment_id     bigint unsigned                    null comment '设备ID',
    part_code        varchar(100)                       not null comment '设备部位编码',
    part_name        varchar(100)                       not null comment '设备部位名称',
    repair_team      varchar(50)                        null comment '检修班组(字典)',
    operate_team     varchar(50)                        null comment '操作班组(字典)',
    repair_strategy  varchar(50)                        null comment '维修策略(字典)',
    importance_level varchar(50)                        null comment '设备部位重要度(字典)',
    part_type        varchar(50)                        null comment '设备部位类别(字典)',
    maintainer       varchar(64)                        null comment '设备部位维护人',
    creator_id       bigint unsigned                    null comment '创建人ID',
    created_at       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_equipment_part_creator_equipment_part
        unique (creator_id, equipment_id, part_code),
    constraint fk_equipment_part_equipment_id
        foreign key (equipment_id) references equipment (id)
            on update cascade on delete cascade
)
    comment '设备部位表' row_format = DYNAMIC;

create index idx_device_code
    on equipment_part (device_code);

create index idx_equipment_part_creator_id
    on equipment_part (creator_id);

create index idx_equipment_part_equipment_id
    on equipment_part (equipment_id);

-- ----------------------------
-- Table structure for material_code
-- ----------------------------

DROP TABLE IF EXISTS `material_code`;
create table material_code
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
    constraint uk_material_code_creator_code
        unique (creator_id, material_code)
)
    comment '物料代码管理表' collate = utf8mb4_general_ci;

create index idx_material_category
    on material_code (material_major_category, material_sub_category);

create index idx_material_name
    on material_code (material_name);

create index idx_status
    on material_code (status);

-- ----------------------------
-- Table structure for id_location
-- ----------------------------

DROP TABLE IF EXISTS `id_location`;
create table id_location
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
    constraint uk_id_location_user_code
        unique (creator_username, id_location_code)
)
    comment 'ID位置管理表' collate = utf8mb4_general_ci;

create index idx_id_location_creator_username
    on id_location (creator_username);

-- ----------------------------
-- Table structure for supplier_manage
-- ----------------------------

DROP TABLE IF EXISTS `supplier_manage`;
create table supplier_manage
(
    id                bigint auto_increment comment '主键ID'
        primary key,
    supplier_category varchar(64)  null comment '供应商分类',
    supplier_code     varchar(64)  not null comment '供应商代码',
    supplier_name     varchar(128) not null comment '供应商名称',
    address           varchar(255) null comment '地址',
    contact_person    varchar(64)  null comment '联系人',
    contact_id        varchar(64)  null comment '联系人电话/标识',
    contact_email     varchar(128) null comment '联系人邮箱',
    fax               varchar(64)  null comment '传真',
    creator_id        bigint       null comment '创建人ID',
    creator_username  varchar(64)  null comment '创建人',
    creator_name      varchar(64)  null comment '创建人姓名',
    created_at        date         null comment '创建日期',
    constraint uk_supplier_creator_code
        unique (creator_id, supplier_code)
)
    comment '供应商管理表' collate = utf8mb4_general_ci;

create index idx_supplier_category
    on supplier_manage (supplier_category);

create index idx_supplier_manage_creator_id
    on supplier_manage (creator_id);

create index idx_supplier_name
    on supplier_manage (supplier_name);

-- ----------------------------
-- Table structure for daily_inspection_frequency
-- ----------------------------
DROP TABLE IF EXISTS `daily_inspection_frequency`;
create table daily_inspection_frequency
(
    id                        bigint auto_increment comment '主键ID'
        primary key,
    daily_inspection_table_id bigint                             null comment '所属日常巡检表ID',
    frequency_code            varchar(50)                        not null comment '日常巡检频次编号',
    area_device_name          varchar(100)                       not null comment '巡检区域设备/部位名称',
    inspection_content        varchar(100)                       null comment '巡检内容',
    inspection_standard       varchar(100)                       null comment '巡检判定标准',
    data_type                 varchar(50)                        null comment '数据类别（定性/定量）',
    cycle_unit                varchar(50)                        null comment '巡检周期单位（天、周、月、班）',
    frequency_value           int                                null comment '巡检周期值',
    month_date_set            datetime                           null comment '月日期设定',
    week_date_set             varchar(50)                        null comment '周日期设定（无、星期一、星期二、星期三...星期日）',
    shifts_set                varchar(50)                        null comment '班次设定（无、白班、夜班、早班、中班、晚班）',
    maximums                  decimal(10, 2)                     null comment '上限',
    minimums                  decimal(10, 2)                     null comment '下限',
    creator_id                bigint unsigned                    null comment '创建人ID',
    created_at                datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at                datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint fk_dif_table_id
        foreign key (daily_inspection_table_id) references daily_inspection_table (id)
            on update cascade on delete cascade
)
    comment '日常巡检频次表' row_format = DYNAMIC;

create index idx_dif_table_id
    on daily_inspection_frequency (daily_inspection_table_id);

-- ----------------------------
-- Table structure for daily_inspection_table
-- ----------------------------
DROP TABLE IF EXISTS `daily_inspection_table`;
create table daily_inspection_table
(
    id                 bigint auto_increment comment '主键ID'
        primary key,
    table_code         varchar(50)                        not null comment '日常巡检表编号',
    table_name         varchar(100)                       not null comment '日常巡检表名称',
    production_line_id bigint unsigned                    not null comment '作业线id',
    shift_mode         varchar(100)                       null comment '班次模式(01-四班二运转 02-四班三运转 03-甲乙两班制)',
    Implementer        varchar(100)                       null comment '实施人',
    status             varchar(100)                       null comment '状态(01-正常 02-异常)',
    creator_id         bigint unsigned                    null comment '创建人id',
    created_at         datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at         datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    row_format = DYNAMIC;

-- ----------------------------
-- Table structure for inspection_route
-- ----------------------------
DROP TABLE IF EXISTS `inspection_route`;
create table inspection_route
(
    id                 bigint auto_increment comment '主键ID'
        primary key,
    production_line_id bigint unsigned                    not null comment '所属生产线ID',
    route_code         varchar(100)                       not null comment '路线编码',
    route_name         varchar(100)                       not null comment '路线名称',
    creator_id         bigint unsigned                    null comment '创建人ID',
    created_at         datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at         datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '设备点检路线表' row_format = DYNAMIC;

-- ----------------------------
-- Table structure for inspection_route_device
-- ----------------------------
DROP TABLE IF EXISTS `inspection_route_device`;
create table inspection_route_device
(
    id             bigint auto_increment comment '主键ID'
        primary key,
    route_code     varchar(100)                       not null comment '路线编码',
    route_name     varchar(100)                       not null comment '路线名称',
    equipment_code varchar(50)                        not null comment '设备编码',
    equipment_name varchar(100)                       not null comment '设备名称',
    sort           int      default 0                 null comment '排序',
    creator_id     bigint unsigned                    null comment '创建人ID',
    created_at     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '路线点检设备表' row_format = DYNAMIC;

-- ----------------------------
-- Table structure for inspection_standard
-- ----------------------------
DROP TABLE IF EXISTS `inspection_standard`;
create table inspection_standard
(
    id                    bigint auto_increment comment '主键ID'
        primary key,
    inspection_name       varchar(100)                       not null comment '点检标准名称',
    part_code             varchar(100)                       not null comment '设备部位编码',
    part_name             varchar(100)                       not null comment '设备部位名称',
    implementation_cycle  int                                null comment '实施周期',
    cycle_unit            varchar(100)                       null comment '周期单位（天、周、月、年）',
    data_type             varchar(100)                       null comment '数据类型（数值、时间波形、长数据(频道波形)、高频段(频谱波形)、频带指标、观察量、仪操量、手操量）',
    signal_type           varchar(100)                       null comment '信号类型（速度、加速度、位移、压力、温度、温差、电流、电压、频率、亮度、功率、光强、流量、扭矩、转速、观察量、长度）',
    implementation_method varchar(100)                       null comment '实施方法（五感、仪器、量具）',
    inspection_content    varchar(255)                       null comment '点检内容',
    inspection_part       varchar(100)                       null comment '点检部位',
    qualitative_standard  varchar(255)                       null comment '定性标准',
    standard_category     varchar(100)                       null comment '标准类别（定量、定性）',
    quantitative_standard varchar(255)                       null comment '定量标准',
    unit_of_measurement   varchar(100)                       null comment '计量单位（毫米
(㎜)、摄氏度(℃)、兆帕(MPa)）',
    profession            varchar(50)                        null comment '专业（01-机械专业 02-电气专业 03-仪表专业 04-过程控制专业）',
    upper_limit           decimal(10, 2)                     null comment '上限',
    lower_limit           decimal(10, 2)                     null comment '下限',
    id_location_code      varchar(64)                        null comment 'ID位置编码',
    id_location_name      varchar(128)                       null comment 'ID位置',
    creator_id            bigint unsigned                    null comment '创建人ID',
    created_at            datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at            datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '设备点检标准表' row_format = DYNAMIC;

-- ----------------------------
-- Table structure for lubrication_standard
-- ----------------------------
DROP TABLE IF EXISTS `lubrication_standard`;
create table lubrication_standard
(
    id              bigint auto_increment comment '主键ID'
        primary key,
    part_code       varchar(100)                       not null comment '设备部位编码',
    part_name       varchar(100)                       not null comment '设备部位名称',
    standard_code   varchar(100)                       not null comment '润滑标准编码',
    standard_name   varchar(100)                       not null comment '润滑标准名称',
    method          varchar(100)                       not null comment '润滑方式',
    profession      varchar(100)                       not null comment '专业',
    oil_feed_type   varchar(100)                       not null comment '给油类型',
    feed_point      varchar(100)                       not null comment '加油点',
    oil_models      varchar(100)                       not null comment '油质型号',
    oil_volume      varchar(100)                       not null comment '油量(升)',
    next_check_time datetime                           not null comment '下次润滑日期',
    creator_id      bigint unsigned                    null comment '创建人ID',
    created_at      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '润滑标准表' row_format = DYNAMIC;


DROP TABLE IF EXISTS `inspection_record`;
create table inspection_record
(
    id                    bigint auto_increment comment '主键ID'
        primary key,
    inspection_standard_id bigint                           null comment '点检标准ID',
    production_line_id    bigint unsigned                   not null comment '生产线ID',
    route_id              bigint                            null comment '点检路线ID',
    route_code            varchar(100)                      null comment '路线编号',
    route_name            varchar(100)                      null comment '路线名称',
    plan_date             varchar(32)                       not null comment '计划日期',
    current_result        varchar(10)                       null comment '当前结果',
    abnormal_type         varchar(10)                       null comment '异常类型',
    abnormal_phenomenon   varchar(500)                      null comment '异常现象',
    plan_source           varchar(32)                       null comment '计划来源',
    standard_code         varchar(100)                      null comment '标准编号',
    part_code             varchar(100)                      not null comment '设备部位编码',
    part_name             varchar(100)                      null comment '设备部位名称',
    inspection_part       varchar(100)                      null comment '点检部位',
    inspection_content    varchar(255)                      null comment '点检内容',
    completed_at          varchar(32)                       null comment '完工日期',
    implementation_cycle  int                               null comment '周期',
    cycle_unit            varchar(100)                      null comment '周期单位',
    completion_flag       varchar(10)                       null comment '完工标记',
    qualitative_standard  varchar(255)                      null comment '定性标准',
    standard_category     varchar(100)                      null comment '标准类别',
    quantitative_standard varchar(255)                      null comment '定量标准',
    unit_of_measurement   varchar(100)                      null comment '计量单位',
    upper_limit           varchar(255)                      null comment '上限',
    lower_limit           varchar(255)                      null comment '下限',
    id_location_code      varchar(64)                       null comment 'ID位置编码',
    id_location_name      varchar(128)                      null comment 'ID位置',
    abnormal_contact_sheet varchar(100)                     null comment '异常联络单',
    creator_id            bigint unsigned                   null comment '创建人ID',
    created_at            datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at            datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '点检实绩表' row_format = DYNAMIC;

create index idx_inspection_record_line_date
    on inspection_record (production_line_id, plan_date);

create index idx_inspection_record_route
    on inspection_record (route_id);

-- ----------------------------
-- Table structure for abnormality_deal
-- ----------------------------
DROP TABLE IF EXISTS `abnormality_deal`;
create table abnormality_deal
(
    id                     bigint auto_increment comment '主键ID'
        primary key,
    abnormality_id         bigint       not null comment '异常记录ID',
    processor              varchar(64)  null comment '处理人',
    process_method         varchar(10)  null comment '处理方式：01正常，02复检，03转检修',
    process_date           varchar(32)  null comment '处理日期',
    review_date            varchar(32)  null comment '复检日期',
    abnormal_occurred_date varchar(32)  null comment '异常发生日期',
    process_opinion        varchar(500) null comment '处理/受审意见',
    created_at             varchar(32)  null comment '创建时间',
    updated_at             varchar(32)  null comment '更新时间',
    constraint uk_abnormality_deal_abnormality_id
        unique (abnormality_id),
    constraint fk_abnormality_deal_abnormality_id
        foreign key (abnormality_id) references abnormality_record (id)
            on delete cascade
)
    comment '异常处理表' collate = utf8mb4_general_ci;

create index idx_process_method
    on abnormality_deal (process_method);



-- ----------------------------
-- Table structure for abnormality_record
-- ----------------------------
DROP TABLE IF EXISTS `abnormality_record`;
create table abnormality_record
(
    id                   bigint auto_increment comment '主键ID'
        primary key,
    abnormal_code        varchar(64)              not null comment '异常单编号',
    device_unit_code     varchar(64)              null comment '设备部位编码',
    source               varchar(64)              null comment '来源',
    abnormal_location    varchar(100)             null comment '异常位置',
    abnormal_type        varchar(10)              null comment '异常类型：01磨损，02松动，03精度超标，04开裂，05损坏，06其他',
    safety_output        varchar(10)              null comment '安全输出：01是，02否',
    reporter             varchar(64)              null comment '报出人',
    abnormal_description varchar(500)             null comment '异常现象',
    report_date          varchar(32)              null comment '报出日期',
    status               varchar(10) default '00' not null comment '状态：00待提交，10待处理，20已处理，30已同意',
    handler              varchar(64)              null comment '处理人工号',
    handler_name         varchar(64)              null comment '处理人姓名',
    handler_date         varchar(32)              null comment '处理日期',
    creator_username     varchar(64)              null comment '创建人工号',
    creator_name         varchar(64)              null comment '创建人姓名',
    created_at           varchar(32)              null comment '创建时间',
    updated_at           varchar(32)              null comment '更新时间',
    constraint uk_abnormal_code
        unique (abnormal_code)
)
    comment '异常管理记录表' collate = utf8mb4_general_ci;

create index idx_abnormal_type
    on abnormality_record (abnormal_type);

create index idx_device_unit_code
    on abnormality_record (device_unit_code);

create index idx_report_date
    on abnormality_record (report_date);

create index idx_status
    on abnormality_record (status);

-- ----------------------------
-- Table structure for warehouse_location
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_location`;
create table warehouse_location
(
    id                 bigint auto_increment comment '主键ID'
        primary key,
    production_line_id bigint unsigned not null comment '归属生产线ID',
    area_code          varchar(50)     not null comment '库区',
    location_code      varchar(50)     not null comment '库位',
    keeper_username    varchar(50)     not null comment '库管员username',
    keeper_real_name   varchar(50)     not null comment '库管员姓名real_name',
    creator_id         bigint          null comment '创建人ID',
    created_at         varchar(32)     null comment '创建日期',
    updated_at         varchar(32)     null comment '更新时间',
    constraint uk_warehouse_location_line_area_location_creator
        unique (production_line_id, area_code, location_code, creator_id),
    constraint fk_warehouse_location_line_id
        foreign key (production_line_id) references production_line (id)
)
    comment '库区库位表' collate = utf8mb4_general_ci;

create index idx_warehouse_location_keeper_username
    on warehouse_location (keeper_username);

create index idx_warehouse_location_line_id
    on warehouse_location (production_line_id);


-- ----------------------------
-- Table structure for inbound_request
-- ----------------------------
DROP TABLE IF EXISTS `inbound_request`;
create table inbound_request
(
    id                    bigint auto_increment comment '主键ID'
        primary key,
    production_line_id    bigint unsigned             not null comment '所属生产线ID',
    inbound_no            varchar(64)                 not null comment '入库单号',
    material_code_id      bigint                      not null comment '物料ID',
    material_code         varchar(100)                not null comment '物料代码',
    material_name         varchar(100)                not null comment '物料名称',
    model_specification   varchar(200)                null comment '型号规格',
    warehouse_location_id bigint                      not null comment '库区库位ID',
    area_code             varchar(50)                 not null comment '库区',
    location_code         varchar(50)                 not null comment '库位',
    inbound_qty           int                         not null comment '入库数量',
    unit_price            decimal(18, 2) default 0.00 not null comment '单价',
    inbound_amount        decimal(18, 2) default 0.00 not null comment '入库金额',
    inbound_status        varchar(30)                 not null comment '入库状态：00-待确认入库，10-已确认入库',
    inventory_property    varchar(30)                 not null comment '库存属性',
    inbound_type          varchar(30)                 not null comment '入库类型',
    inbound_date          varchar(20)                 not null comment '入库日期',
    creator_id            bigint                      null comment '创建人ID',
    creator_username      varchar(50)                 null comment '创建人username快照',
    creator_name          varchar(50)                 null comment '创建人姓名快照',
    created_at            varchar(32)                 null comment '创建日期',
    updated_at            varchar(32)                 null comment '更新时间',
    constraint uk_inbound_request_no
        unique (inbound_no),
    constraint fk_inbound_request_line_id
        foreign key (production_line_id) references production_line (id),
    constraint fk_inbound_request_location_id
        foreign key (warehouse_location_id) references warehouse_location (id),
    constraint fk_inbound_request_material_id
        foreign key (material_code_id) references material_code (id)
)
    comment '入库申请表' collate = utf8mb4_general_ci;

create index idx_inbound_request_line_id
    on inbound_request (production_line_id);

create index idx_inbound_request_location_id
    on inbound_request (warehouse_location_id);

create index idx_inbound_request_material_id
    on inbound_request (material_code_id);

create index idx_inbound_request_status
    on inbound_request (inbound_status);

-- ----------------------------
-- Table structure for maintenance_standard
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_standard`;
create table maintenance_standard
(
    id                     bigint auto_increment comment '主键ID'
        primary key,
    standard_code          varchar(64)  not null comment '标准项目编码',
    part_code              varchar(100) not null comment '设备部位编码',
    part_name              varchar(100) null comment '设备部位名称',
    item_name              varchar(100) not null comment '标准项目名称',
    risk_factor            varchar(255) null comment '风险因素',
    safety_measure         varchar(255) null comment '安全防范措施',
    work_category          varchar(100) null comment '工事分类',
    work_content           varchar(255) null comment '工事内容',
    acceptance_level       varchar(100) null comment '验收等级',
    maintenance_category   varchar(100) null comment '检修分类',
    profession             varchar(100) null comment '专业',
    cycle_value            int          null comment '周期值',
    cycle_unit             varchar(50)  null comment '周期单位',
    last_completion_date   date         null comment '上次完工日期',
    next_schedule_date     date         null comment '下次排程日期',
    maintainer_id          bigint       null comment '设备维护人ID',
    maintainer_name        varchar(100) null comment '设备维护人姓名',
    maintainer_username    varchar(100) null comment '设备维护人用户名',
    maintenance_start_time date         null comment '维护开始时间',
    maintenance_end_time   date         null comment '维护结束时间',
    created_at             datetime     null comment '创建时间',
    updated_at             datetime     null comment '更新时间',
    constraint uk_maintenance_standard_code
        unique (standard_code)
)
    comment '检修标准主表' collate = utf8mb4_general_ci;

create index idx_maintainer_part
    on maintenance_standard (maintainer_id, part_code);

create index idx_maintainer_standard_code
    on maintenance_standard (maintainer_id, standard_code);

create index idx_maintenance_item_name
    on maintenance_standard (item_name);

create index idx_maintenance_part_code
    on maintenance_standard (part_code);

-- ----------------------------
-- Table structure for maintenance_standard_spare_part
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_standard_spare_part`
create table maintenance_standard_spare_part
(
    id                    bigint auto_increment comment '主键ID'
        primary key,
    standard_id           bigint       not null comment '检修标准ID',
    material_code_id      bigint       not null comment '物料ID',
    inbound_request_id    bigint       not null comment '入库申请ID',
    material_code         varchar(100) not null comment '物料代码',
    material_name         varchar(200) not null comment '物料名称',
    material_sub_category varchar(100) null comment '物料分类',
    model_specification   varchar(200) null comment '规格型号',
    quantity              int          not null comment '数量',
    quantity_unit         varchar(50)  null comment '数量单位',
    created_at            datetime     null comment '创建时间',
    updated_at            datetime     null comment '更新时间'
)
    comment '检修标准-备件明细表' collate = utf8mb4_general_ci;

create index idx_mssp_standard_id
    on maintenance_standard_spare_part (standard_id);

-- ----------------------------
-- Table structure for maintenance_standard_tool
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_standard_tool`
create table maintenance_standard_tool
(
    id                    bigint auto_increment comment '主键ID'
        primary key,
    standard_id           bigint       not null comment '检修标准ID',
    material_code_id      bigint       not null comment '物料ID',
    inbound_request_id    bigint       not null comment '入库申请ID',
    material_code         varchar(100) not null comment '物料代码',
    material_name         varchar(200) not null comment '物料名称',
    material_sub_category varchar(100) null comment '物料分类',
    model_specification   varchar(200) null comment '规格型号',
    quantity              int          not null comment '数量',
    quantity_unit         varchar(50)  null comment '数量单位',
    created_at            datetime     null comment '创建时间',
    updated_at            datetime     null comment '更新时间'
)
    comment '检修标准-工器具明细表' collate = utf8mb4_general_ci;

create index idx_mst_standard_id
    on maintenance_standard_tool (standard_id);

-- ----------------------------
-- Table structure for maintenance_standard_safety_tag
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_standard_safety_tag`
create table maintenance_ticket_safety_tag
(
    id              bigint auto_increment comment '主键ID'
        primary key,
    ticket_id       bigint       not null comment '工单ID',
    tag_nature      varchar(100) null comment '安全牌性质',
    tag_location    varchar(100) null comment '挂牌位置',
    tag_device_code varchar(100) null comment '挂牌设备编码',
    created_at      varchar(32)  null comment '创建时间',
    updated_at      varchar(32)  null comment '更新时间'
)
    comment '工单安全挂牌明细表';

create index idx_ticket_id
    on maintenance_ticket_safety_tag (ticket_id);


-- ----------------------------
-- Table structure for maintenance_plan
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_plan`;
create table maintenance_plan
(
    id                     bigint auto_increment comment '主键ID'
        primary key,
    plan_code              varchar(64)                      not null comment '计划编号',
    production_line_code   varchar(100)                     not null comment '生产线代码',
    production_line_name   varchar(100)                     not null comment '生产线名称',
    maintenance_category   varchar(100)                     not null comment '检修分类：01-日修/02-定修/03-年修',
    maintenance_start_time varchar(32)                      not null comment '检修开始时间',
    maintenance_end_time   varchar(32)                      not null comment '检修结束时间',
    status                 varchar(100) default '00-待送审' not null comment '状态：00-待送审/10-已送审/20-已通过/30-已申请延期/40-延期已送审/50-延期通过',
    creator_id             bigint                           null comment '创建人ID',
    creator_username       varchar(100)                     null comment '创建人',
    creator_name           varchar(100)                     null comment '创建人姓名',
    created_at             varchar(32)                      null comment '创建时间',
    updated_at             varchar(32)                      null comment '更新时间',
    constraint uk_maintenance_plan_code
        unique (plan_code)
)
    comment '检修计划表';

create index idx_creator_plan_code
    on maintenance_plan (creator_id, plan_code);

create index idx_creator_status
    on maintenance_plan (creator_id, status);

create index idx_maintenance_plan_category
    on maintenance_plan (maintenance_category);

create index idx_maintenance_plan_line_code
    on maintenance_plan (production_line_code);

create index idx_maintenance_plan_status
    on maintenance_plan (status);

-- ----------------------------
-- Table structure for maintenance_ticket
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_ticket`;
create table maintenance_ticket
(
    id                     bigint auto_increment comment '主键ID'
        primary key,
    ticket_code            varchar(100)                     not null comment '工单编码',
    ticket_name            varchar(100)                     not null comment '工单名称',
    status                 varchar(100) default '00-待提交' not null comment '状态',
    maintenance_plan_name  varchar(100)                     null comment '检修保养计划',
    operation_team         varchar(100)                     null comment '操作班组',
    task_type              varchar(100)                     null comment '任务类型',
    production_impact      varchar(100)                     null comment '是否影响生产',
    entrust_type           varchar(100)                     null comment '委托类型',
    maintenance_resource   varchar(255)                     null comment '检修资源',
    maintenance_content    varchar(500)                     null comment '维修内容',
    abnormal_phenomenon    varchar(500)                     null comment '异常理象',
    handling_opinion       varchar(500)                     null comment '处理/受理意见',
    plan_id                bigint                           null comment '检修计划ID快照',
    plan_code              varchar(100)                     null comment '计划编号快照',
    maintenance_category   varchar(100)                     null comment '检修分类',
    maintenance_start_time varchar(32)                      null comment '检修开始时间',
    maintenance_end_time   varchar(32)                      null comment '检修结束时间',
    standard_id            bigint                           null comment '检修标准ID快照',
    standard_code          varchar(100)                     null comment '标准项目编码快照',
    part_code              varchar(100)                     not null comment '设备部位编码快照',
    part_name              varchar(100)                     null comment '设备部位名称快照',
    item_name              varchar(100)                     not null comment '项目名称快照',
    profession             varchar(100)                     null comment '专业',
    risk_factor            varchar(255)                     null comment '风险因素',
    safety_measure         varchar(255)                     null comment '安全防范措施',
    work_category          varchar(100)                     null comment '工事分类',
    acceptance_level       varchar(100)                     null comment '验收等级',
    creator_id             bigint                           null comment '创建人ID',
    creator_username       varchar(100)                     null comment '创建人账号',
    creator_name           varchar(100)                     null comment '创建人姓名',
    created_at             varchar(32)                      null comment '创建时间',
    updated_at             varchar(32)                      null comment '更新时间'
)
    comment '工单主表';

create index idx_creator_status
    on maintenance_ticket (creator_id, status);

create index idx_creator_ticket_code
    on maintenance_ticket (creator_id, ticket_code);

create index idx_part_code
    on maintenance_ticket (part_code);

create index idx_plan_code
    on maintenance_ticket (plan_code);

create index idx_standard_code
    on maintenance_ticket (standard_code);

create index idx_status
    on maintenance_ticket (status);

create index idx_ticket_code
    on maintenance_ticket (ticket_code);

-- ----------------------------
-- Table structure for maintenance_ticket_spare_part
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_ticket_spare_part`;
create table maintenance_ticket_spare_part
(
    id                    bigint auto_increment comment '主键ID'
        primary key,
    ticket_id             bigint       not null comment '工单ID',
    material_code_id      bigint       null comment '物料代码ID快照',
    inbound_request_id    bigint       null comment '入库申请ID快照',
    material_code         varchar(100) null comment '物料代码',
    material_name         varchar(100) null comment '物料名称',
    material_sub_category varchar(100) null comment '物料分类',
    model_specification   varchar(100) null comment '规格型号',
    quantity              int          null comment '数量',
    quantity_unit         varchar(50)  null comment '数量单位',
    created_at            varchar(32)  null comment '创建时间',
    updated_at            varchar(32)  null comment '更新时间'
)
    comment '工单备件明细表';

create index idx_ticket_id
    on maintenance_ticket_spare_part (ticket_id);

-- ----------------------------
-- Table structure for maintenance_ticket_tool
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_ticket_tool`;
create table maintenance_ticket_tool
(
    id                    bigint auto_increment comment '主键ID'
        primary key,
    ticket_id             bigint       not null comment '工单ID',
    material_code_id      bigint       null comment '物料代码ID快照',
    inbound_request_id    bigint       null comment '入库申请ID快照',
    material_code         varchar(100) null comment '物料代码',
    material_name         varchar(100) null comment '物料名称',
    material_sub_category varchar(100) null comment '物料分类',
    model_specification   varchar(100) null comment '规格型号',
    quantity              int          null comment '数量',
    quantity_unit         varchar(50)  null comment '数量单位',
    created_at            varchar(32)  null comment '创建时间',
    updated_at            varchar(32)  null comment '更新时间'
)
    comment '工单工器具明细表';

create index idx_ticket_id
    on maintenance_ticket_tool (ticket_id);

-- ----------------------------
-- Table structure for maintenance_ticket_safety_tag
-- ----------------------------
DROP TABLE IF EXISTS `maintenance_ticket_safety_tag`;
create table maintenance_ticket_safety_tag
(
    id              bigint auto_increment comment '主键ID'
        primary key,
    ticket_id       bigint       not null comment '工单ID',
    tag_nature      varchar(100) null comment '安全牌性质',
    tag_location    varchar(100) null comment '挂牌位置',
    tag_device_code varchar(100) null comment '挂牌设备编码',
    created_at      varchar(32)  null comment '创建时间',
    updated_at      varchar(32)  null comment '更新时间'
)
    comment '工单安全挂牌明细表';

create index idx_ticket_id
    on maintenance_ticket_safety_tag (ticket_id);


-- ----------------------------
-- Table structure for failure_record
-- ----------------------------
DROP TABLE IF EXISTS `failure_record`;
create table failure_record
(
    id                  bigint auto_increment comment '主键ID'
        primary key,
    failure_code        varchar(64)                           not null comment '故障编号',
    device_part_code    varchar(64)                           null comment '设备部位编码',
    device_part_name    varchar(100)                          null comment '设备部位名称',
    failure_name        varchar(100)                          null comment '故障名称',
    failure_phenomenon  varchar(500)                          null comment '故障现象',
    failure_description varchar(1000)                         null comment '故障描述',
    failure_notice_type varchar(100)                          null comment '故障事故通知单类型',
    failure_type        varchar(10)                           null comment '故障分类',
    failure_start_time  datetime                              null comment '故障开始时间',
    status              varchar(10) default '00'              not null comment '状态：00-待提交 01-待处理 02-待审批 03-已同意',
    creator_username    varchar(50)                           not null comment '创建人username',
    creator_name        varchar(50)                           not null comment '创建人姓名',
    created_at          datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at          datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_failure_code
        unique (failure_code)
)
    comment '故障录入主表';

create index idx_failure_creator_status
    on failure_record (creator_username, status);

create index idx_failure_part_code
    on failure_record (device_part_code);


-- ----------------------------
-- Table structure for failure_deal
-- ----------------------------
DROP TABLE IF EXISTS `failure_deal`;
create table failure_deal
(
    id                  bigint auto_increment comment '主键ID'
        primary key,
    failure_id          bigint                             not null comment '故障主表ID',
    severity_level      varchar(32)                        null comment '事件等级',
    process_time        datetime                           null comment '处理时间',
    process_description varchar(1000)                      null comment '处理说明',
    created_at          datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at          datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_failure_deal_failure_id
        unique (failure_id),
    constraint fk_failure_deal_failure_id
        foreign key (failure_id) references failure_record (id)
            on delete cascade
)
    comment '故障处理表';


SET FOREIGN_KEY_CHECKS = 1;
