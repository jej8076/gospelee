-- 반응(기도할게요 등) 공통 테이블 (PostgreSQL, prod 는 ddl-auto: none)
CREATE TABLE IF NOT EXISTS reaction (
    uid           BIGSERIAL PRIMARY KEY,
    reaction_type VARCHAR(30) NOT NULL,
    target_type   VARCHAR(30) NOT NULL,
    target_id     BIGINT      NOT NULL,
    account_uid   BIGINT      NOT NULL,
    insert_user   VARCHAR(255),
    update_user   VARCHAR(255),
    insert_time   TIMESTAMP,
    update_time   TIMESTAMP,
    CONSTRAINT uk_reaction UNIQUE (reaction_type, target_type, target_id, account_uid)
);
-- 대상별 수 집계는 유니크 인덱스(reaction_type, target_type, target_id, account_uid)로 처리된다
-- 내가 표시한 것만 모아보기 (최신순)
CREATE INDEX IF NOT EXISTS idx_reaction_account
    ON reaction (account_uid, reaction_type, uid DESC);
