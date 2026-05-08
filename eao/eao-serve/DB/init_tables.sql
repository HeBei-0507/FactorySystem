create database device_manage_system;
use device_manage_system;

drop table if exists sys_user;
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

INSERT INTO sys_user (username, real_name, password_hash, phone, status, is_deleted, created_at, updated_at)
VALUES ('test_admin', '测试管理员', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', 1, 0, DEFAULT, DEFAULT);

drop table if exists production_line;
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

INSERT INTO production_line (line_code, line_name, description, creator_id)
VALUES ('RC-AUTO-001', '遥控器自动化生产线', '用于遥控器产品的自动化组装与测试生产线', 1);

INSERT INTO production_line (line_code, line_name, description, creator_id)
VALUES ('RC-AUTO-002', '手机自动化生产线', '主要负责产品组装的生产线', 1);

INSERT INTO production_line (line_code, line_name, description, creator_id)
VALUES ('RC-AUTO-003', '笔记本自动化生产线', '主要负责产品组装的生产线', 1);

drop table if exists device_unit;
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
    constraint uk_device_unit_code
        unique (unit_code),
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

drop table  if exists equipment;
create table equipment
(
    id                   bigint unsigned auto_increment comment '主键ID'
        primary key,
    unit_code            varchar(50)                        not null comment '设备单元编码',
    unit_name            varchar(100)                       not null comment '设备单元名称',
    equipment_code       varchar(50)                        not null comment '设备编码',
    equipment_name       varchar(100)                       not null comment '设备名称',
    maintainer_name      varchar(100)                       null comment '设备维护人',
    equipment_category   varchar(100)                       null comment '设备类别',
    equipment_importance varchar(100)                       null comment '设备重要度',
    repair_strategy      varchar(100)                       null comment '维修策略',
    overhaul_team        varchar(100)                       null comment '检修班组',
    act_team             varchar(100)                       null comment '操作班组',
    created_at           datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_equipment_code
        unique (equipment_code),
    constraint fk_equipment_unit_code
        foreign key (unit_code) references device_unit (unit_code)
            on update cascade on delete cascade
)
    comment '设备表';

create index idx_unit_code
    on equipment (unit_code);



-- =====================================
-- 4. 设备部位表
-- =====================================
DROP TABLE IF EXISTS equipment_part;
CREATE TABLE equipment_part (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    device_code VARCHAR(100) NOT NULL COMMENT '设备编码',
    device_name VARCHAR(100) COMMENT '设备名称',

    part_code VARCHAR(100) NOT NULL COMMENT '设备部位编码',
    part_name VARCHAR(100) NOT NULL COMMENT '设备部位名称',

    repair_team VARCHAR(50) COMMENT '检修班组(字典)',
    operate_team VARCHAR(50) COMMENT '操作班组(字典)',
    repair_strategy VARCHAR(50) COMMENT '维修策略(字典)',

    importance_level VARCHAR(50) COMMENT '设备部位重要度(字典)',
    part_type VARCHAR(50) COMMENT '设备部位类别(字典)',

    maintainer VARCHAR(64) COMMENT '设备部位维护人',

    creator_id         bigint unsigned                    null comment '创建人ID',

    created_at           datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',

    UNIQUE KEY uk_part_code(part_code),
    INDEX idx_device_code(device_code)
) COMMENT='设备部位表';


-- =====================================
-- 5. 润滑标准表
-- ==========
DROP TABLE IF EXISTS lubrication_standard;
CREATE TABLE lubrication_standard (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    part_code VARCHAR(100) NOT NULL COMMENT '设备部位编码',
    part_name VARCHAR(100) NOT NULL COMMENT '设备部位名称',

    standard_code VARCHAR(100) NOT NULL COMMENT '润滑标准编码',
    standard_name VARCHAR(100) NOT NULL COMMENT '润滑标准名称',


    method VARCHAR(100) NOT NULL COMMENT '润滑方式', -- 01-手工润滑 02-滴注润滑 03-飞溅润滑 04-油环与油链润滑 05-油绳与油垫润滑 06-自润滑
    profession VARCHAR(100) NOT NULL COMMENT '专业', -- 01-机械专业 02-电气专业 03-仪表专业 04-过程控制专业
    oil_feed_type VARCHAR(100) NOT NULL COMMENT '给油类型', -- 01-补漏 02-换油 03-油质化验

    feed_point VARCHAR(100) NOT NULL COMMENT '加油点',
    oil_models VARCHAR(100) NOT NULL COMMENT '油质型号',
    oil_volume VARCHAR(100) NOT NULL COMMENT '油量(升)',

    next_check_time datetime NOT NULL COMMENT '下次润滑日期',

    creator_id         bigint unsigned                    null comment '创建人ID',

    created_at           datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) COMMENT='润滑标准表';


-- =====================================
-- 6.点检标准表
-- ==========
DROP TABLE IF EXISTS inspection_standard;
CREATE TABLE `inspection_standard` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `inspection_name` VARCHAR(100) NOT NULL COMMENT '点检标准名称',

    `part_code` VARCHAR(100) NOT NULL COMMENT '设备部位编码',
    `part_name` VARCHAR(100) NOT NULL COMMENT '设备部位名称',

    `implementation_cycle` INT COMMENT '实施周期',
    `cycle_unit` VARCHAR(100) COMMENT '周期单位（天、周、月、年）',
    `data_type` VARCHAR(100) COMMENT '数据类型（数值、时间波形、长数据(频道波形)、高频段(频谱波形)、频带指标、观察量、仪操量、手操量）',
    `signal_type` VARCHAR(100) COMMENT '信号类型（速度、加速度、位移、压力、温度、温差、电流、电压、频率、亮度、功率、光强、流量、扭矩、转速、观察量、长度）',
    `implementation_method` VARCHAR(100) COMMENT '实施方法（五感、仪器、量具）',
    `inspection_content` VARCHAR(255) COMMENT '点检内容',
    `inspection_part` VARCHAR(100) COMMENT '点检部位',
    `qualitative_standard` VARCHAR(255) COMMENT '定性标准',
    `standard_category` VARCHAR(100) COMMENT '标准类别（定量、定性）',
    `quantitative_standard` VARCHAR(255) COMMENT '定量标准',
    `unit_of_measurement` VARCHAR(100) COMMENT '计量单位（毫米
(㎜)、摄氏度(℃)、兆帕(MPa)）',
    `profession` VARCHAR(50) COMMENT '专业（01-机械专业 02-电气专业 03-仪表专业 04-过程控制专业）',
    `upper_limit` DECIMAL(10, 2) COMMENT '上限',
    `lower_limit` DECIMAL(10, 2) COMMENT '下限',
    creator_id         bigint unsigned                    null comment '创建人ID',

    created_at           datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
   ) COMMENT='设备点检标准表';

-- =====================================
-- 7.点检路线表
-- ==========
DROP TABLE IF EXISTS inspection_route;
create table inspection_route(
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    production_line_id bigint unsigned                    not null comment '所属生产线ID',

    route_code VARCHAR(100) NOT NULL COMMENT '路线编码',
    route_name VARCHAR(100) NOT NULL COMMENT '路线名称',

    creator_id         bigint unsigned                    null comment '创建人ID',

    created_at           datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'

) comment='设备点检路线表';

-- =====================================
-- 8.路线点检设备表
-- ==========
DROP TABLE IF EXISTS inspection_route_device;
create table inspection_route_device(
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    route_code VARCHAR(100) NOT NULL COMMENT '路线编码',
    route_name VARCHAR(100) NOT NULL COMMENT '路线名称',

    equipment_code       varchar(50)                        not null comment '设备编码',
    equipment_name       varchar(100)                       not null comment '设备名称',

    sort int default 0 comment '排序',

    creator_id         bigint unsigned                    null comment '创建人ID',

    created_at           datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment='路线点检设备表';

-- =====================================
-- 9.日常巡检表
-- ==========
DROP TABLE IF EXISTS daily_inspection;
create table daily_inspection_table(
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    table_code       varchar(50)                        not null comment '日常巡检表编号',
    table_name       varchar(100)                       not null comment '日常巡检表名称',

    production_line_id bigint unsigned                    not null comment '作业线id',
    shift_mode varchar(100) comment '班次模式(01-四班二运转 02-四班三运转 03-甲乙两班制)',
    Implementer varchar(100) comment '实施人',
    status varchar(100) comment '状态(01-正常 02-异常)',
    creator_id bigint unsigned comment '创建人id',
    created_at           datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'

);

-- =====================================
-- 10.日常巡检频次
-- ==========
DROP TABLE IF EXISTS daily_inspection_frequency;
create table daily_inspection_frequency(
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',

    frequency_code       varchar(50)                        not null comment '日常巡检频次编号',
    area_device_name       varchar(100)                       not null comment '巡检区域设备/部位名称',
    inspection_content varchar(100) comment '巡检内容',
    inspection_standard varchar(100) comment '巡检判定标准',
    data_type varchar(50) comment '数据类别（定性/定量）',
    cycle_unit varchar(50) comment '巡检周期单位（天、周、月、班）',
    frequency_value int comment '巡检周期值',
    month_date_set datetime comment '月日期设定',
    week_date_set varchar(50) comment '周日期设定（无、星期一、星期二、星期三...星期日）',
    shifts_set varchar(50) comment '班次设定（无、白班、夜班、早班、中班、晚班）',
    maximums DECIMAL(10, 2) comment '上限',
    minimums DECIMAL(10, 2) comment '下限',

    creator_id         bigint unsigned                    null comment '创建人ID',

    created_at           datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '日常巡检频次表';

-- =====================================
-- 11.故障录入主表
-- ==========
DROP TABLE IF EXISTS failure_record;
CREATE TABLE failure_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    failure_code VARCHAR(64) NOT NULL COMMENT '故障编号',
    device_part_code VARCHAR(64) COMMENT '设备部位编码',
    device_part_name VARCHAR(100) COMMENT '设备部位名称',
    failure_name VARCHAR(100) COMMENT '故障名称',
    failure_phenomenon VARCHAR(500) COMMENT '故障现象',
    failure_description VARCHAR(1000) COMMENT '故障描述',
    failure_notice_type VARCHAR(100) COMMENT '故障事故通知单类型',
    failure_type VARCHAR(10) COMMENT '故障分类',
    failure_start_time DATETIME COMMENT '故障开始时间',
    status VARCHAR(10) NOT NULL DEFAULT '00' COMMENT '状态：00-待提交 01-待处理 02-待审核 03-已通过',
    creator_username VARCHAR(50) NOT NULL COMMENT '创建人username',
    creator_name VARCHAR(50) NOT NULL COMMENT '创建人姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_failure_code (failure_code),
    KEY idx_failure_creator_status (creator_username, status),
    KEY idx_failure_part_code (device_part_code)
) COMMENT='故障录入主表';

-- =====================================
-- 12.故障处理表
-- ==========
DROP TABLE IF EXISTS failure_deal;
CREATE TABLE failure_deal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    failure_id BIGINT NOT NULL COMMENT '故障主表ID',
    severity_level VARCHAR(32) COMMENT '事件等级',
    process_time DATETIME COMMENT '处理时间',
    process_description VARCHAR(1000) COMMENT '处理说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_failure_deal_failure_id (failure_id),
    CONSTRAINT fk_failure_deal_failure_id FOREIGN KEY (failure_id) REFERENCES failure_record (id) ON DELETE CASCADE
) COMMENT='故障处理表';