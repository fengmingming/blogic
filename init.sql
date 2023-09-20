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