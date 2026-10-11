-- 묵상 나눔 기능 운영 DB 마이그레이션 (PostgreSQL, prod 는 ddl-auto: none)
-- 서버 이미지 배포 전/직후에 반드시 먼저 실행해야 한다 (account.nickname 이 없으면 계정 조회가 실패함)

ALTER TABLE account ADD COLUMN IF NOT EXISTS nickname VARCHAR(20);

CREATE TABLE IF NOT EXISTS journal_share (
    uid          BIGSERIAL PRIMARY KEY,
    journal_uid  BIGINT       NOT NULL,
    account_uid  BIGINT       NOT NULL,
    token        VARCHAR(32)  NOT NULL,
    reference    VARCHAR(500),
    verse_text   TEXT,
    content      TEXT,
    status       VARCHAR(10)  NOT NULL,
    insert_user  VARCHAR(255),
    update_user  VARCHAR(255),
    insert_time  TIMESTAMP,
    update_time  TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_journal_share_token ON journal_share (token);
CREATE INDEX IF NOT EXISTS idx_journal_share_journal ON journal_share (journal_uid);

CREATE TABLE IF NOT EXISTS journal_share_comment (
    uid          BIGSERIAL PRIMARY KEY,
    share_uid    BIGINT         NOT NULL,
    account_uid  BIGINT         NOT NULL,
    content      VARCHAR(1000)  NOT NULL,
    status       VARCHAR(10)    NOT NULL,
    insert_user  VARCHAR(255),
    update_user  VARCHAR(255),
    insert_time  TIMESTAMP,
    update_time  TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_journal_share_comment_share ON journal_share_comment (share_uid);

CREATE TABLE IF NOT EXISTS content_report (
    uid           BIGSERIAL PRIMARY KEY,
    reporter_uid  BIGINT       NOT NULL,
    target_type   VARCHAR(10)  NOT NULL,
    target_uid    BIGINT       NOT NULL,
    reason        VARCHAR(30)  NOT NULL,
    detail        VARCHAR(300),
    status        VARCHAR(10)  NOT NULL,
    insert_user   VARCHAR(255),
    update_user   VARCHAR(255),
    insert_time   TIMESTAMP,
    update_time   TIMESTAMP,
    CONSTRAINT uk_content_report UNIQUE (reporter_uid, target_type, target_uid)
);

CREATE TABLE IF NOT EXISTS account_block (
    uid          BIGSERIAL PRIMARY KEY,
    blocker_uid  BIGINT NOT NULL,
    blocked_uid  BIGINT NOT NULL,
    insert_user  VARCHAR(255),
    update_user  VARCHAR(255),
    insert_time  TIMESTAMP,
    update_time  TIMESTAMP,
    CONSTRAINT uk_account_block UNIQUE (blocker_uid, blocked_uid)
);
