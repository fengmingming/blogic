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
    key company_id (company_id)
)engine=INNODB default charset=utf8mb4 comment='部门';

create table blogic.user_department (
    id bigint not null auto_increment comment '主键',
    user_id bigint not null comment '用户id',
    department_id bigint not null comment '部门id',
    primary key (id),
    key user_id (user_id)
)engine=INNODB default charset=utf8mb4 comment='用户部门关系表';


create table blogic.product (
    id bigint not null auto_increment comment '产品id',
    company_id bigint not null comment '公司id',
    product_name varchar(254) not null comment '产品名称',
    product_desc text null comment '产品描述',
    create_user_id bigint not null comment '创建用户id',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0否 1已删除',
    primary key (id),
    key company_id (company_id)
)engine=INNODB default charset=utf8mb4 comment='产品表';

create table blogic.product_member (
	id bigint not null auto_increment comment '',
    product_id bigint not null comment '产品id',
    user_id bigint not null comment '用户id',
    primary key (id),
    unique key product_user (product_id, user_id)
)engine=INNODB default charset=utf8mb4 comment='产品成员表';

create table blogic.requirement(
    id bigint not null auto_increment comment '需求id',
    product_id bigint not null comment '产品id',
    requirement_name varchar(254) not null comment '需求名称',
    requirement_sources varchar(254) null comment '需求来源',
    requirement_desc text null comment '需求描述',
    requirement_status int not null comment '需求状态 10已确认 20已规划 30开发中 40已实现 50已发布 60已关闭',
    create_user_id bigint not null comment '创建用户id',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0否 1已删除',
    primary key (id),
    key product_id (product_id)
)engine=INNODB default charset=utf8mb4 comment='需求表';


create table blogic.iteration (
    id bigint not null auto_increment comment '迭代id',
    product_id bigint not null comment '产品id',
    version_code varchar(50) not null comment '迭代号',
    name varchar(254) not null comment '迭代名称',
    scheduled_start_time datetime null comment '迭代开始时间',
    scheduled_end_time datetime null comment '迭代结束时间',
    status int not null comment '迭代状态 10未开始 20进行中 30已完成',
    create_user_id bigint not null comment '创建用户id',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0否 1已删除',
    primary key (id),
    key product_id (product_id)
)engine=INNODB default charset=utf8mb4 comment='迭代';

create table blogic.iteration_member(
    id bigint not null auto_increment comment '',
    iteration_id bigint not null comment '迭代id',
    user_id bigint not null comment '用户id',
    primary key (id),
    unique key iteration_user(iteration_id,user_id)
)engine=INNODB default charset=utf8mb4 comment='迭代成员';

create table blogic.iteration_requirement(
    id bigint not null auto_increment comment '',
    iteration_id bigint not null comment '迭代id',
    requirement_id bigint not null comment '需求id',
    primary key (id),
    unique key iteration_requirement_id(iteration_id,requirement_id)
)engine=INNODB default charset=utf8mb4 comment='迭代中的需求';

create table blogic.task(
    id bigint not null auto_increment comment '任务id',
    iteration_id bigint null comment '迭代id',
    product_id bigint not null comment '产品id',
    task_name varchar(254) not null comment '任务名称',
    task_desc text null comment '任务描述',
    status int not null comment '任务状态 10未开始 20进行中 30已完成 40已取消',
    current_user_id bigint null comment '指派给',
    complete_user_id bigint null comment '完成用户id',
    start_time datetime null comment '任务开始时间',
    complete_time datetime null comment '任务完成时间',
    final_time datetime null comment '任务结束时间',
    priority int not null default 4 comment '1最高4最低',
    overall_time int null comment '计划时间',
    consume_time int null comment '消耗时间',
    create_user_id bigint not null comment '创建用户id',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0否 1已删除',
    primary key (id),
    key iteration_id(iteration_id),
    key current_user_id(current_user_id),
    key create_user_id(create_user_id),
    key complete_user_id(complete_user_id)
)engine=INNODB default charset=utf8mb4 comment='任务表';


create table blogic.test_case(
    id bigint not null auto_increment comment '用例id',
    iteration_id bigint null comment '迭代id',
	requirement_id bigint null comment '相关需求id',
	product_id bigint not null comment '产品id',
    title varchar(254) not null comment '用例标题',
    priority int not null default 4 comment '1最高4最低',
    precondition varchar(1000) null comment '前置条件',
    owner_user_id bigint null comment '用例负责人',
    smoke tinyint not null default 0 comment '0否1是',
    status int not null default 10 comment '10未开始 20测试中 30被阻塞 40已完成',
    complete_time datetime null comment '完成时间',
    create_user_id bigint not null comment '创建用户id',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0否 1已删除',
    primary key (id),
    key owner_user_id (owner_user_id),
    key iteration_id_requirement_id (iteration_id, requirement_id)
)engine=INNODB default charset=utf8mb4 comment='测试用例';

create table blogic.test_case_step(
	id bigint not null auto_increment comment '步骤id',
    test_case_id bigint not null comment '用例id',
    number varchar(50) not null comment '编号',
    step varchar(1000) not null comment '步骤',
    expected_result varchar(1000) not null comment '预期结果',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0否 1已删除',
    primary key (id),
    key test_case_id (test_case_id)
)engine=INNODB default charset=utf8mb4 comment='测试用例步骤';


create table blogic.bug (
	id bigint not null auto_increment comment 'bug的id',
    test_case_id bigint null comment '测试用例',
    requirement_id bigint null comment '需求id',
    iteration_id bigint null comment '迭代id',
    product_id bigint not null comment '产品id',
    iteration_version varchar(50) null comment '影响版本号',
	title varchar(100) not null comment '标题',
    bug_type int not null comment 'bug类型 字典bug_type',
    env int not null comment '环境编号 字典bug_env',
    device varchar(100) null comment '设备',
    repro_steps text null comment '重现步骤',
    status int not null default 10 comment '状态',
    severity int not null default 4 comment '严重程度 1最大 4最小 字典 bug_severity',
    priority int not null default 4 comment '优先级 1最大 4最小 字典 bug_priority',
    current_user_id bigint null comment '当前指派人',
    fix_user_id bigint null comment 'bug解决人',
    fix_solution int null comment '解决方案 字典 bug_fix_solution',
    fix_version varchar(50) null comment '解决方案',
    create_user_id bigint not null comment '创建用户id',
    create_time datetime not null comment '创建时间',
    update_time datetime null comment '修改时间',
    deleted tinyint not null default 0 comment '0否 1已删除',
    primary key (id),
    key create_user_id (current_user_id),
    key fix_user_id (fix_user_id),
    key product_id (product_id)
)engine=INNODB default charset=utf8mb4 comment='bug';

create table blogic.change_record (
    id bigint not null auto_increment comment '',
    primary_key bigint not null comment '主键id',
    key_type int not null comment '主键类型',
    oper_user_id bigint null comment '操作用户id',
    oper_desc varchar(254) not null comment '操作描述',
    note text null comment '备注',
    create_time datetime not null comment '创建时间',
    primary key (id),
    key primary_key_type (primary_key, key_type)
)engine=INNODB default charset=utf8mb4 comment='变更记录';

create table blogic.dict (
    id bigint not null auto_increment comment '主键',
    dict_type varchar(100) not null comment '标识类型',
    code int not null comment '编码',
    code_desc varchar(50) not null comment '描述',
    locale varchar(10) not null default 'zh-CN' comment '语言',
    create_time datetime not null comment '创建时间',
    primary key (id),
	unique key dict_type_code_locale (dict_type, code, locale)
)engine=INNODB default charset=utf8mb4 comment='字典表';


