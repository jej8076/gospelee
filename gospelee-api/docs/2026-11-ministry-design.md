# 모임(Ministry) 설계 (초안)

워십팀·선교단체 같은 모임이 교회와 별개로 커뮤니티를 만들 수 있게 한다. 교회와의 가장 큰 차이는 **한 사람이 여러 모임에 속할 수 있다**는 점이다. 앱 안의 이름은 임시로 "모임"으로 부른다 (API·DB 는 `ministry`).

## 원칙

1. **소속은 분리, 콘텐츠는 공유.** 모임과 멤버십은 `ecclesia` 와 완전히 별개의 테이블로 만든다. 공지·갤러리·기도 쪽지 같은 콘텐츠 테이블은 `(organization_type, organization_id)` 한 쌍으로 어느 조직의 것인지 가리켜 교회와 모임이 같은 코드를 쓴다 (`Announcement` 가 이미 이 방식).
2. **교회 기능은 건드리지 않는다.** `Account.ecclesiaUid`, JWT 필터의 교회 조회, 기존 교회 API 는 그대로 둔다. 모임 API 는 경로에 모임 id 를 받는다 (`/ministry/{id}/...`).
3. **DDL 은 추가만.** 운영 DB 는 `ddl-auto: none` 이므로 `CREATE ... IF NOT EXISTS`, `ADD COLUMN`, 제약 완화(`DROP NOT NULL`)만 쓴다. 기존 컬럼은 코드를 다 옮길 때까지 지우지 않는다.

## 확정된 결정

| 항목 | 결정 |
|---|---|
| 개설 | 로그인한 누구나. 한 사람이 **대표인 모임은 최대 3개** |
| 가입 | 초대 링크 중심. 승인 필요 여부는 모임이 선택. 한 사람이 **가입한 모임은 최대 10개** |
| 모임 종류 | 구분하지 않는다 |
| 대표 탈퇴 | 탈퇴자가 양도 대상을 지정할 수 있다. 지정하지 않으면 **가입일이 가장 오래된 멤버**가 대표. 멤버가 없으면 **모임과 데이터를 삭제** |
| 저장 용량 | 모임당 기본 **100MB** |
| 푸시 | 제목에 모임 이름을 표기한다 |
| 1차 기능 | 공지, 푸시, 갤러리, 기도 쪽지 |
| 앱 위치 | 교회 탭 상단의 조직 이름(`포도교회 ⌄`)을 눌러 내 교회와 내 모임을 전환 |

## 단계

| 단계 | 내용 |
|---|---|
| **1a** | 모임·멤버십·초대·공지·푸시·신고, 앱 전환 UI, 관리자 웹 최소 기능(목록·정지) |
| **1b** | 갤러리 (콘텐츠 테이블의 조직 일반화, 모임 용량 한도) |
| **1c** | 기도 쪽지 (모임별 설정, 뽑기 범위) |

각 단계를 따로 배포·확인한다. 1b, 1c 는 1a 가 안정된 뒤에 시작한다.

## 데이터 모델 (1a)

```sql
CREATE TABLE IF NOT EXISTS ministry (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(30)  NOT NULL,
    description         VARCHAR(200),
    -- ACTIVE / SUSPENDED (운영자 정지)
    status              VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    -- 초대 링크 코드. 교회와 같은 방식으로 생성·재발급
    invite_code         VARCHAR(32),
    invite_auto_approve BOOLEAN      NOT NULL DEFAULT FALSE,
    storage_limit_bytes BIGINT       NOT NULL DEFAULT 104857600,   -- 100MB
    storage_used_bytes  BIGINT       NOT NULL DEFAULT 0,
    insert_user         VARCHAR(255),
    update_user         VARCHAR(255),
    insert_time         TIMESTAMP,
    update_time         TIMESTAMP,
    CONSTRAINT uk_ministry_invite_code UNIQUE (invite_code)
);

CREATE TABLE IF NOT EXISTS ministry_member (
    id           BIGSERIAL PRIMARY KEY,
    ministry_id  BIGINT      NOT NULL,
    account_uid  BIGINT      NOT NULL,
    -- OWNER(대표) / MANAGER(운영진) / MEMBER
    role         VARCHAR(10) NOT NULL DEFAULT 'MEMBER',
    -- ACTIVE / PENDING(가입 승인 대기)
    status       VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    -- 대표 승계 기준. 승인된 시각(ACTIVE 가 된 시각)을 넣는다
    joined_at    TIMESTAMP,
    insert_time  TIMESTAMP,
    CONSTRAINT uk_ministry_member UNIQUE (ministry_id, account_uid)
);
-- 한 모임에 대표는 한 명만 (PostgreSQL 부분 유니크 인덱스)
CREATE UNIQUE INDEX IF NOT EXISTS uk_ministry_owner
    ON ministry_member (ministry_id) WHERE role = 'OWNER';
-- 내 모임 목록, 가입·개설 개수 제한 조회
CREATE INDEX IF NOT EXISTS idx_ministry_member_account
    ON ministry_member (account_uid, status);
```

대표를 `ministry` 테이블 컬럼으로 따로 두지 않고 멤버십의 `role = OWNER` 로 표현한다. 대표 교체가 한 테이블 갱신으로 끝나고, 부분 유니크 인덱스가 "대표 한 명"을 보장한다.

### 개수 제한

- **대표 3개**: `role = 'OWNER'` 인 멤버십 수. 모임을 만들 때, 그리고 대표를 넘겨받을 때 검사한다 (이미 3개의 대표인 사람에게는 양도할 수 없다).
- **가입 10개**: `ACTIVE` 와 `PENDING` 멤버십 수. 승인 대기를 세지 않으면 신청만 무한히 쌓을 수 있다.
- 제한 검사와 삽입은 같은 트랜잭션에서 한다. 같은 사람이 동시에 두 번 요청해도 한도를 넘지 않도록 계정 단위로 잠근다.

## 콘텐츠 공유 (1b, 1c)

| 테이블 | 변경 |
|---|---|
| `announcement` | 변경 없음. `organization_type = MINISTRY`, `organization_id = ministry.id`. `OrganizationType.MINISTRY` 의 `hasEntity` 를 `true` 로 바꾼다 |
| `gallery_folder`, `gallery_image` | `organization_type`(기본 `ECCLESIA`), `organization_id` 추가 후 기존 `ecclesia_uid` 값으로 채운다. `ecclesia_uid` 의 `NOT NULL` 을 푼다 |
| `prayer_setting`, `prayer_card` 와 관련 테이블 | 같은 방식. 설정은 조직마다 한 행 (`uk_prayer_setting_ecclesia` 를 `(organization_type, organization_id)` 유니크로 보강) |

```sql
-- 예: 갤러리 (1b). 기존 행은 모두 ECCLESIA 로 채워진다
ALTER TABLE gallery_folder ADD COLUMN IF NOT EXISTS organization_type VARCHAR(20) NOT NULL DEFAULT 'ECCLESIA';
ALTER TABLE gallery_folder ADD COLUMN IF NOT EXISTS organization_id BIGINT;
UPDATE gallery_folder SET organization_id = ecclesia_uid WHERE organization_id IS NULL;
ALTER TABLE gallery_folder ALTER COLUMN ecclesia_uid DROP NOT NULL;
```

- 모임의 행은 `ecclesia_uid` 가 `NULL` 이므로, 기존 교회 쿼리(`ecclesia_uid = ?`)에는 잡히지 않는다. 코드를 옮기기 전에도 교회 쪽 결과가 달라지지 않는다.
- 새 코드는 `(organization_type, organization_id)` 로 조회한다. 옮긴 뒤 `ecclesia_uid` 컬럼 정리는 별도 작업으로 한다.
- 저장 용량은 `Ecclesia.addStorageUsedBytes` 를 직접 부르는 곳(`GalleryImageServiceImpl`, `AnnouncementStorageGuard`)이 있다. 교회와 모임이 같은 인터페이스(예: `StorageOwner`)를 구현하게 해서 한도 검사와 사용량 갱신을 공유한다. 모임 한도는 100MB 고정(검증 전 교회와 같은 값)이다.
- 기도 쪽지는 교회 설정이 관리자 웹에서 켜진다. 모임은 **대표/운영진이 앱에서** 켜고 끈다. 사용자가 여러 모임에 속하므로 뽑기·요약은 현재 선택한 조직 기준으로만 동작한다.

## 권한

| 동작 | 대표 | 운영진 | 멤버 |
|---|:-:|:-:|:-:|
| 공지 작성·수정·삭제 | O | O | X |
| 초대 링크 보기·재발급·승인 설정 | O | O | X |
| 가입 신청 승인·거절 | O | O | X |
| 멤버 내보내기 | O | 멤버만 | X |
| 운영진 지정·해제 | O | X | X |
| 모임 정보 수정 | O | O | X |
| 대표 양도 | O | X | X |
| 모임 삭제 | O | X | X |
| 공지 조회, 갤러리·기도 쪽지 이용 | O | O | O |
| 신고 | O | O | O |

권한은 모임별 `role` 로 판정한다. 계정 전역의 `RoleType`(목사 등)은 쓰지 않는다. 모든 모임 API 는 먼저 "이 계정이 이 모임의 `ACTIVE` 멤버인가"를 검사한다 (아니면 존재 여부도 알 수 없는 응답).

## 탈퇴와 대표 승계

대표가 모임을 떠나는 경우의 규칙이다. 앱 계정 탈퇴로 모든 모임을 떠나는 경우에도 같은 규칙을 모임마다 적용한다.

1. 탈퇴하는 대표가 양도 대상(같은 모임의 `ACTIVE` 멤버)을 지정하면, 그 사람이 대표가 된다. 대상이 이미 대표 3개라면 거절하고 다른 사람을 고르게 한다.
2. 지정하지 않으면 남은 `ACTIVE` 멤버 중 `joined_at` 이 가장 이른 사람이 대표가 된다. 이때도 대표 3개 한도를 넘는 사람은 건너뛰고 다음 사람에게 넘긴다.
3. 남은 멤버가 없으면(또는 모두 한도에 걸리면) 모임을 삭제한다. 공지, 갤러리 이미지(실제 파일 포함), 기도 쪽지, 멤버십을 모두 지운다.
4. `PENDING` 상태의 신청자는 승계 대상에서 제외한다. 모임이 삭제되면 신청도 함께 지운다.
5. 대표가 아닌 멤버의 탈퇴는 멤버십만 삭제한다. 그 모임에 쓴 공지와 사진은 남는다. 기도 쪽지는 기존 규칙대로 작성자 탈퇴 시 삭제한다.

처리는 한 트랜잭션에서 하고, 같은 모임에 동시에 탈퇴가 들어와도 대표가 없는 상태나 대표가 둘인 상태가 생기지 않도록 모임 행을 잠근다. 파일 삭제는 트랜잭션이 커밋된 뒤에 한다 (실패해도 DB 는 일관되게 유지하고 재시도할 수 있게 기록한다).

앱의 계정 탈퇴 화면에는 "대표로 있는 모임"과 그 처리 결과(양도 대상 선택, 자동 승계, 삭제 예정)를 보여 주고 확인을 받아야 한다.

## 초대와 가입

교회의 `invite_code` / `invite_auto_approve` 로직을 모임용으로 옮긴다.

- 링크: `/ministry-invite/join?code=` (딥링크 별칭 포함). 코드는 대표/운영진이 재발급할 수 있다.
- 자동 승인이 켜져 있으면 가입 즉시 `ACTIVE`, 꺼져 있으면 `PENDING` 으로 저장하고 대표·운영진에게 푸시를 보낸다.
- 이미 멤버이거나 신청 중이면 중복 처리하지 않는다. 가입 10개 한도를 넘으면 거절한다. 정지된 모임은 가입할 수 없다.
- 초대 정보 조회(`info/{code}`)에는 모임 이름과 멤버 수만 내려 주고, 멤버 목록은 주지 않는다.

## 푸시

- 제목 형식: `[모임이름] 공지 제목`. 길면 모임 이름을 줄인다.
- payload 에 `organizationType`, `organizationId` 를 넣어, 누르면 앱이 해당 모임으로 전환해 공지를 연다.
- 수신자는 모임의 `ACTIVE` 멤버 중 푸시 토큰이 있는 계정이다. 푸시 이력 테이블에도 조직을 남긴다.
- 모임의 푸시 그룹은 1차에 없다 (전체 멤버 대상만).

## API 초안 (1a)

기존 규칙대로 컨텍스트 경로 `/api`, 오류는 `{code, message}` 와 HTTP 200. **성공 응답은 반드시 본문을 가진다** (앱이 빈 본문을 실패로 판단한다).

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/ministry` | 모임 만들기 (이름, 소개). 대표 3개 한도 검사 |
| GET | `/ministry/mine` | 내 모임 목록 (역할, 승인 대기 여부) |
| GET | `/ministry/{id}` | 모임 정보, 내 역할 |
| PATCH | `/ministry/{id}` | 이름·소개 수정 |
| DELETE | `/ministry/{id}` | 모임 삭제 (대표) |
| POST | `/ministry/{id}/leave` | 탈퇴. 본문 `{successorUid?}` |
| GET | `/ministry/{id}/members` | 멤버 목록 |
| PATCH | `/ministry/{id}/members/{uid}/role` | 운영진 지정·해제, 대표 양도 |
| DELETE | `/ministry/{id}/members/{uid}` | 내보내기 |
| GET / POST / PATCH | `/ministry/{id}/invite` · `/invite/regenerate` · `/invite/settings` | 초대 링크 |
| GET | `/ministry/invite/info/{code}` | 초대 정보 |
| POST | `/ministry/join/invite` | 초대 코드로 가입 |
| GET | `/ministry/{id}/requests` | 가입 신청 목록 |
| PATCH | `/ministry/{id}/requests/{uid}` | 신청 승인·거절 |

공지는 기존 공지 API 에 `organizationType=MINISTRY`, `organizationId` 를 받게 확장한다 (현재 클라이언트 조회·작성 경로가 교회 기준인지 구현 전에 확인한다). 신고는 `ContentReport.targetType` 에 값을 추가해 재사용한다.

## 앱

- **현재 조직 상태**: `(type, id)` 를 앱이 보관(SharedPreferences)하고, 교회 탭 전체가 이 값을 기준으로 그려진다. 앱을 다시 켜도 마지막 선택을 유지하되, 소속이 사라졌으면 교회 또는 첫 모임으로 돌아간다.
- **전환 UI**: 앱바의 `포도교회 ⌄` 를 누르면 시트가 열린다. 구성은 "내 교회", "내 모임들"(승인 대기 표시 포함), "모임 만들기", "초대 코드로 가입".
- **탭 구성**: 교회는 공지사항 / 주보 / 사진 / QT 묵상, 모임은 공지 / 사진 / 기도 쪽지 (1a 에서는 공지, 이후 단계에서 추가).
- **교회가 없는 사용자**: 가입 안내 화면 대신 "교회 찾기"와 "내 모임 / 모임 만들기"를 함께 보여 준다.
- **푸시로 진입**: payload 의 조직으로 전환한 뒤 해당 화면을 연다.
- 새 화면: 모임 만들기, 모임 정보·멤버 관리, 가입 신청 관리, 초대 링크 공유, 대표 양도 선택, 탈퇴 확인.

## 관리자 웹 (1a 최소)

슈퍼 관리자가 모임 목록(이름, 멤버 수, 사용량, 상태)을 보고 `SUSPENDED` 로 정지·해제한다. 신고가 누적된 모임을 찾는 용도다. 교회 관리 화면(`/manage/*`)은 그대로 둔다.

## 테스트

- 개설 3개·가입 10개 한도 (경계값, 동시 요청)
- 승계: 양도 지정, 자동 승계(가입일 순서), 한도에 걸린 후보 건너뛰기, 멤버 없음 → 삭제, 동시 탈퇴
- 권한 표의 각 칸 (멤버가 운영 API 호출 시 거절)
- 초대: 자동 승인/대기, 중복, 정지된 모임, 코드 재발급 후 이전 코드 무효
- 응답에 내려가면 안 되는 값(초대 코드는 대표·운영진만) 회귀 방지
- 푸시 제목의 모임 이름 표기

## 추가로 확정한 것

6. **자동 승계 후보**: 운영진(`MANAGER`) 중 `joined_at` 이 가장 이른 사람을 먼저, 운영진이 없으면 일반 멤버 중에서. 대표 3개 한도에 걸린 사람은 건너뛴다.
7. **대표의 삭제**: 멤버가 남아 있어도 삭제할 수 있다. 앱에서 모임 이름을 입력해 확인받는다.
8. **내보낸 사람의 재가입**: 초대 링크로 다시 가입할 수 있으나, 모임이 자동 승인이어도 내보낸 이력이 있으면 `PENDING` 으로 두어 대표·운영진이 승인한다 (차단 목록 테이블은 두지 않는다).
9. **모임 이름 중복**: 허용한다.
10. **정지된 모임**: 멤버는 읽기만 가능. 작성·업로드·푸시·초대 변경은 막는다.
11. **용어**: 앱 화면 문구는 "모임". 코드와 API 는 `ministry`.

## 추후 확장: 모임의 교회 소속

지금은 만들지 않는다. 필요해지면 연결 테이블로 추가한다 (`ministry_ecclesia(ministry_id, ecclesia_uid, status, requested_at, decided_at)`). 모임이 교회에 소속을 요청하고 교회 관리자가 승인하는 흐름이며, 기존 테이블을 바꾸지 않아도 되도록 모임과 교회를 완전히 분리해 둔 것이다.
