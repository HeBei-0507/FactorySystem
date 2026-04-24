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

INSERT INTO production_line (line_code, line_name, description, creator_id)
VALUES ('RC-AUTO-001', '遥控器自动化生产线', '用于遥控器产品的自动化组装与测试生产线', 1);

INSERT INTO production_line (line_code, line_name, description, creator_id)
VALUES ('RC-AUTO-002', '手机自动化生产线', '主要负责产品组装的生产线', 1);

INSERT INTO production_line (line_code, line_name, description, creator_id)
VALUES ('RC-AUTO-003', '笔记本自动化生产线', '主要负责产品组装的生产线', 1);
