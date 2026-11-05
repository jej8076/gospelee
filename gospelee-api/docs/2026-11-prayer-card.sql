-- 기도 쪽지 (PostgreSQL, prod 는 ddl-auto: none)
-- 교인이 기도제목을 익명/이름 공개로 올리고, 같은 교회 성도가 뽑아서 기도해 주는 콘텐츠.
-- 교회 관리자가 prayer_setting.enabled 를 켜야 성도에게 공개된다.

-- 교회별 설정. 행이 없으면 아래 기본값(가장 보수적인 값)으로 본다.
CREATE TABLE IF NOT EXISTS prayer_setting (
    id                   BIGSERIAL PRIMARY KEY,
    ecclesia_uid         BIGINT      NOT NULL,
    enabled              BOOLEAN     NOT NULL DEFAULT FALSE,
    -- 작성 방식: ANON_ONLY / NAME_ONLY / AUTHOR_CHOICE
    author_mode          VARCHAR(20) NOT NULL DEFAULT 'AUTHOR_CHOICE',
    max_length           INT         NOT NULL DEFAULT 200,
    -- 노출 기간 선택지 (쉼표 구분, 일 단위. 0 = 상시). 예: '7,30,0'
    allowed_expiry_days  VARCHAR(30) NOT NULL DEFAULT '7,30',
    -- 작성 대상: APPROVED_ONLY(교회 가입 승인된 성도) / ALL_MEMBERS
    who_can_write        VARCHAR(20) NOT NULL DEFAULT 'APPROVED_ONLY',
    -- 공개 범위: CHURCH_ONLY / GROUP_ALLOWED
    scope_mode           VARCHAR(20) NOT NULL DEFAULT 'CHURCH_ONLY',
    -- 기도한 사람을 작성자에게: HIDDEN(인원수만) / NAMED / RESPONDER_CHOICE
    responder_visibility VARCHAR(20) NOT NULL DEFAULT 'HIDDEN',
    -- 익명 작성자를 관리자에게: NEVER / REPORTED_ONLY / ALWAYS
    admin_sees_anon      VARCHAR(20) NOT NULL DEFAULT 'NEVER',
    -- 검수: POST_REPORT(신고 시 숨김) / PRE_APPROVAL(승인 후 노출)
    moderation_mode      VARCHAR(20) NOT NULL DEFAULT 'POST_REPORT',
    auto_hide_threshold  INT         NOT NULL DEFAULT 3,
    cheer_enabled        BOOLEAN     NOT NULL DEFAULT TRUE,
    -- 하루 뽑기 횟수 (0 = 무제한)
    daily_draw_limit     INT         NOT NULL DEFAULT 5,
    summary_push         BOOLEAN     NOT NULL DEFAULT TRUE,
    insert_user          VARCHAR(255),
    update_user          VARCHAR(255),
    insert_time          TIMESTAMP,
    update_time          TIMESTAMP,
    CONSTRAINT uk_prayer_setting_ecclesia UNIQUE (ecclesia_uid)
);

-- 설정 변경 이력. 프라이버시 정책이 걸려 있어 누가 언제 무엇을 바꿨는지 남긴다.
CREATE TABLE IF NOT EXISTS prayer_setting_history (
    id             BIGSERIAL PRIMARY KEY,
    ecclesia_uid   BIGINT       NOT NULL,
    changed_by     BIGINT       NOT NULL,
    -- 바뀐 항목만 {"필드": {"from": ..., "to": ...}} 형태로 저장
    changes        TEXT         NOT NULL,
    changed_at     TIMESTAMP    NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_prayer_setting_history_ecclesia
    ON prayer_setting_history (ecclesia_uid, changed_at DESC);

CREATE TABLE IF NOT EXISTS prayer_card (
    id                 BIGSERIAL PRIMARY KEY,
    ecclesia_uid       BIGINT       NOT NULL,
    -- 내부용. 익명 카드여도 저장하고 API 응답에서는 내보내지 않는다
    author_uid         BIGINT       NOT NULL,
    content            VARCHAR(300) NOT NULL,
    -- ANON / NAME
    display_mode       VARCHAR(10)  NOT NULL,
    -- CHURCH / GROUP
    scope              VARCHAR(10)  NOT NULL DEFAULT 'CHURCH',
    group_id           BIGINT,
    -- NULL 이면 상시
    expires_at         TIMESTAMP,
    -- PENDING(승인 대기) / ACTIVE / ANSWERED / HIDDEN
    status             VARCHAR(10)  NOT NULL,
    -- 카드를 올린 시점의 정책 스냅샷. 이후 교회 설정이 바뀌어도 이 카드에는 적용하지 않는다
    responder_visible  VARCHAR(20)  NOT NULL DEFAULT 'HIDDEN',
    admin_sees_anon    VARCHAR(20)  NOT NULL DEFAULT 'NEVER',
    response_count     INT          NOT NULL DEFAULT 0,
    report_count       INT          NOT NULL DEFAULT 0,
    insert_user        VARCHAR(255),
    update_user        VARCHAR(255),
    insert_time        TIMESTAMP,
    update_time        TIMESTAMP
);
-- 뽑기: 교회 + 노출 중 + 기도받은 횟수가 적은 순
CREATE INDEX IF NOT EXISTS idx_prayer_card_draw
    ON prayer_card (ecclesia_uid, status, response_count);
CREATE INDEX IF NOT EXISTS idx_prayer_card_author ON prayer_card (author_uid);

CREATE TABLE IF NOT EXISTS prayer_card_response (
    id             BIGSERIAL PRIMARY KEY,
    card_id        BIGINT      NOT NULL,
    responder_uid  BIGINT      NOT NULL,
    -- 기도자가 이름 공개를 골랐는지 (카드 스냅샷이 RESPONDER_CHOICE/NAMED 일 때만 의미가 있다)
    show_name      BOOLEAN     NOT NULL DEFAULT FALSE,
    -- 정해진 응원 문구 코드 (없으면 NULL)
    cheer_code     VARCHAR(20),
    insert_time    TIMESTAMP,
    CONSTRAINT uk_prayer_card_response UNIQUE (card_id, responder_uid)
);

-- 하루 뽑기 횟수 계산용 (카드를 뽑아 본 기록)
CREATE TABLE IF NOT EXISTS prayer_card_draw (
    id           BIGSERIAL PRIMARY KEY,
    card_id      BIGINT    NOT NULL,
    drawer_uid   BIGINT    NOT NULL,
    drawn_at     TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_prayer_card_draw_drawer
    ON prayer_card_draw (drawer_uid, drawn_at DESC);

CREATE TABLE IF NOT EXISTS prayer_card_report (
    id            BIGSERIAL PRIMARY KEY,
    card_id       BIGINT       NOT NULL,
    reporter_uid  BIGINT       NOT NULL,
    reason        VARCHAR(30)  NOT NULL,
    detail        VARCHAR(300),
    -- PENDING / RESOLVED(카드를 내림) / DISMISSED(문제 없음)
    status        VARCHAR(10)  NOT NULL,
    insert_time   TIMESTAMP,
    CONSTRAINT uk_prayer_card_report UNIQUE (card_id, reporter_uid)
);

-- 크리스마스 트리 등 이벤트. 교회별로 켜고 끈다
CREATE TABLE IF NOT EXISTS prayer_tree_event (
    id            BIGSERIAL PRIMARY KEY,
    ecclesia_uid  BIGINT    NOT NULL,
    enabled       BOOLEAN   NOT NULL DEFAULT FALSE,
    start_at      TIMESTAMP,
    end_at        TIMESTAMP,
    -- 점등률을 보여 주는 최소 카드 수 (적은 교회가 쉽게 100% 되는 것을 막는다)
    min_cards     INT       NOT NULL DEFAULT 10,
    insert_user   VARCHAR(255),
    update_user   VARCHAR(255),
    insert_time   TIMESTAMP,
    update_time   TIMESTAMP,
    CONSTRAINT uk_prayer_tree_event_ecclesia UNIQUE (ecclesia_uid)
);
