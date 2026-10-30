-- 푸시 알림 그룹 (PostgreSQL, prod 는 ddl-auto: none)
-- 교회 관리자가 성도를 직접 골라 묶어 두고, 공지·주보 등록 시 그룹에게만 알림을 보낸다.
CREATE TABLE IF NOT EXISTS push_group (
    id            BIGSERIAL PRIMARY KEY,
    ecclesia_uid  BIGINT       NOT NULL,
    name          VARCHAR(30)  NOT NULL,
    description   VARCHAR(100),
    insert_user   VARCHAR(255),
    update_user   VARCHAR(255),
    insert_time   TIMESTAMP,
    update_time   TIMESTAMP,
    CONSTRAINT uk_push_group_ecclesia_name UNIQUE (ecclesia_uid, name)
);

CREATE TABLE IF NOT EXISTS push_group_member (
    id           BIGSERIAL PRIMARY KEY,
    group_id     BIGINT NOT NULL,
    account_uid  BIGINT NOT NULL,
    CONSTRAINT uk_push_group_member UNIQUE (group_id, account_uid)
);
-- 발송 시 그룹 id 로 멤버를 찾는 조회는 유니크 인덱스(group_id, account_uid)로 처리된다
