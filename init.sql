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
)engine=INNODB default charset=utf8mb4 comment='用户在公司中的角色';

create table blogic.im_message (
	id bigint not null auto_increment comment '',
    from_user_id bigint not null comment '发送消息人',
    to_user_id bigint null comment '接受消息人',
    group_id bigint null comment '组id',
    msg_type varchar(10) not null comment '消息类型',
    content text not null comment '消息内容',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '1删除 0 未删除',
    primary key (id),
    key from_user_id (from_user_id),
    key to_user_id (to_user_id),
    key group_id (group_id)
)engine=INNODB default charset=utf8mb4 comment='消息';

create table blogic.im_group (
	id bigint not null auto_increment comment '组id',
    group_name varchar(100) not null comment '组名称',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0未删除 1删除',
    primary key (id)
)engine=INNODB default charset=utf8mb4 comment='组信息';

create table blogic.im_group_member (
	id bigint not null auto_increment comment '主键',
    group_id bigint not null comment '组id',
    user_id bigint not null comment '组成员',
    admin tinyint not null default 0 comment '管理员',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0未删除 1删除',
    primary key (id),
    key group_id (group_id)
)engine=INNODB default charset=utf8mb4 comment='组成员';

create table blogic.department(
    id bigint not null auto_increment comment 'id',
    company_id bigint not null comment '公司id',
    department_name varchar(254) not null comment '部门名称',
    parent_id bigint null comment '父部门id',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0否 1已删除',
    primary key (id),
    key companyId (companyId)
)engine=INNODB default charset=utf8mb4 comment='部门';

create table blogic.user_department (
    id bigint not null auto_increment comment '主键',
    user_id bigint not null comment '用户id',
    department_id bigint not null comment '部门id',
    primary key (id),
    key user_id (user_id)
)engine=INNODB default charset=utf8mb4 comment='用户部门关系表';