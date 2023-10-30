create table gospelee.account
(
    uid         bigint auto_increment
        primary key,
    id          varchar(255) null,
    name        varchar(255) null,
    phone       varchar(255) null,
    insert_user varchar(255) null,
    insert_time datetime(6)  null,
    update_user varchar(255) null,
    update_time datetime(6)  null
);