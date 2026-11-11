-- 모임 (PostgreSQL, prod 는 ddl-auto: none). 설계: 2026-11-ministry-design.md
CREATE TABLE IF NOT EXISTS ministry (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(30)  NOT NULL,
    description         VARCHAR(200),
    status              VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    invite_code         VARCHAR(32),
    invite_auto_approve BOOLEAN      NOT NULL DEFAULT FALSE,
    storage_limit_bytes BIGINT       NOT NULL DEFAULT 104857600,
    storage_used_bytes  BIGINT       NOT NULL DEFAULT 0,
    insert_user         VARCHAR(255),
    update_user         VARCHAR(255),
    insert_time         TIMESTAMP,
    update_time         TIMESTAMP,
    CONSTRAINT uk_ministry_invite_code UNIQUE (invite_code)
);

-- role: OWNER / MANAGER / MEMBER, status: ACTIVE / PENDING / REMOVED(내보낸 이력)
CREATE TABLE IF NOT EXISTS ministry_member (
    id           BIGSERIAL PRIMARY KEY,
    ministry_id  BIGINT      NOT NULL,
    account_uid  BIGINT      NOT NULL,
    role         VARCHAR(10) NOT NULL DEFAULT 'MEMBER',
    status       VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    joined_at    TIMESTAMP,
    insert_time  TIMESTAMP,
    CONSTRAINT uk_ministry_member UNIQUE (ministry_id, account_uid)
);
-- 한 모임에 대표는 한 명만
CREATE UNIQUE INDEX IF NOT EXISTS uk_ministry_owner
    ON ministry_member (ministry_id) WHERE role = 'OWNER';
CREATE INDEX IF NOT EXISTS idx_ministry_member_account
    ON ministry_member (account_uid, status);
