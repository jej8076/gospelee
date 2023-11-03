create table gospelee.account
(
    uid         bigint auto_increment
        primary key,
    id          varchar(30) null,
    phone       varchar(15) null,
    name        varchar(30) null,
    rrn       varchar(13) null,
    insert_user varchar(255) null,
    insert_time datetime(6)  null,
    update_user varchar(255) null,
    update_time datetime(6)  null
);