-- 교회 주보 (PostgreSQL, prod 는 ddl-auto: none)
-- 교회마다 예배 날짜(주일) 하나에 주보 한 건. 첨부 파일은 file_uid 로 file/file_details 와 연결한다.
CREATE TABLE IF NOT EXISTS bulletin (
    id            BIGSERIAL PRIMARY KEY,
    ecclesia_uid  BIGINT      NOT NULL,
    worship_date  DATE        NOT NULL,
    title         VARCHAR(255),
    file_uid      BIGINT,
    open_yn       VARCHAR(1)  NOT NULL DEFAULT 'Y',
    insert_user   VARCHAR(255),
    update_user   VARCHAR(255),
    insert_time   TIMESTAMP,
    update_time   TIMESTAMP,
    CONSTRAINT uk_bulletin_ecclesia_date UNIQUE (ecclesia_uid, worship_date)
);
-- 교회별 최신순 목록은 유니크 인덱스(ecclesia_uid, worship_date)로 처리된다
