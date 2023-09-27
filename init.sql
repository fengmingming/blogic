create table blogic.user (
    id bigint not null auto_increment comment 'id',
    phone varchar(20) not null comment '手机号',
    name varchar(100) null comment '姓名',
    password varchar(100) null comment '密码',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0否 1已删除',
    primary key (id),
    unique key phone (phone)
)engine=INNODB default charset=utf8mb4 comment='用户表';

create table blogic.company (
    id bigint not null auto_increment comment 'id',
    company_name varchar(254) not null comment '',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0否 1已删除',
    primary key (id)
)engine=innodb default charset=utf8mb4 comment='公司';

drop table blogic.user_company_role;
create table blogic.user_company_role (
    id bigint not null auto_increment comment 'id',
    user_id bigint not null comment '用户id',
    company_id bigint not null comment '公司id',
    role varchar(100) not null comment '角色名称',
    admin tinyint not null default 0 comment '0否 1是 是否是公司管理者',
    primary key (id),
    key user_company (user_id,company_id)
)engine=INNODB default charset=utf8mb4 comment '用户在公司中的角色';