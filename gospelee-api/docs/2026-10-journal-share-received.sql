-- 공유받은 묵상 목록 (PostgreSQL, prod 는 ddl-auto: none)
CREATE TABLE IF NOT EXISTS journal_share_received (
    uid              BIGSERIAL PRIMARY KEY,
    account_uid      BIGINT    NOT NULL,
    share_uid        BIGINT    NOT NULL,
    last_viewed_at   TIMESTAMP NOT NULL,
    insert_user      VARCHAR(255),
    update_user      VARCHAR(255),
    insert_time      TIMESTAMP,
    update_time      TIMESTAMP,
    CONSTRAINT uk_journal_share_received UNIQUE (account_uid, share_uid)
);
CREATE INDEX IF NOT EXISTS idx_journal_share_received_account
    ON journal_share_received (account_uid, last_viewed_at DESC);
