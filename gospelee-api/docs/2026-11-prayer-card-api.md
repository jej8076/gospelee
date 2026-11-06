# 기도 쪽지 API 명세 (초안)

DDL: `2026-11-prayer-card.sql`. 오류는 기존 규칙대로 `{code, message}` 와 HTTP 200 으로 내려간다.

## 공통 규칙

- **서버가 최종 판단한다.** 교회 설정 `enabled = false` 이면 성도용 API 는 모두 거절한다 (설정 조회만 예외).
- 모든 조회·변경은 `AuthenticatedUserUtils` 의 로그인 계정과 **그 계정의 교회(ecclesiaUid)** 로 한정한다. 다른 교회 카드는 존재 여부도 알 수 없다 (404 와 같은 응답).
- 작성 권한: 설정 `who_can_write = APPROVED_ONLY` 이면 `ecclesiaStatus = APL` 인 성도만. 기본값이다.
- 탈퇴(`leaveYn = Y`) 계정은 접근 불가. 탈퇴 처리 시 그 계정의 카드·기도 기록을 함께 삭제한다.
- 응답에는 **작성자 uid 를 절대 넣지 않는다.** `display_mode = NAME` 인 카드만 작성자 이름·닉네임을 내려 준다.
- 정책 스냅샷: 카드 생성 시 설정의 `responder_visibility`, `admin_sees_anon` 값을 카드에 복사한다. 이후 설정 변경은 새 카드에만 적용된다.

## 설정

기본값은 행이 없을 때도 동일하다: 꺼짐 / 작성자 선택 / 200자 / 7,30일 / 승인된 성도만 / 교회 전체 / 기도자 비공개 / 익명 작성자 관리자에게도 비공개 / 신고 시 숨김 / 신고 3건 / 응원 문구 사용 / 하루 5회 / 요약 푸시 켜짐.

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| GET | `/prayer/setting` | 교회 성도 | 앱용 설정. 성도에게 필요한 값만 내려 준다 (`enabled`, `authorMode`, `maxLength`, `allowedExpiryDays`, `scopeMode`, `responderVisibility`, `cheerEnabled`, `canWrite`, `treeEvent{enabled,startAt,endAt}`) |
| GET | `/prayer/admin/setting` | 교회 관리자 | 전체 설정 + 최근 변경 이력 |
| PUT | `/prayer/admin/setting` | 교회 관리자 | 설정 저장. 바뀐 항목을 `prayer_setting_history` 에 기록한다 |
| PUT | `/prayer/admin/tree-event` | 교회 관리자 | 트리 이벤트 켜기/끄기, 기간, 최소 카드 수 |

관리자 판정은 기존 `AnnouncementAccessPolicy.canWriteEcclesia` 를 쓴다.

## 카드

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/prayer/cards` | 작성. 본문 `{content, displayMode, expiryDays, scope, groupId?}`. 설정에서 허용하지 않은 값이면 거절. 사전 검수면 `PENDING`, 아니면 `ACTIVE` |
| GET | `/prayer/cards/{id}` | 카드 한 장 보기 (트리에서 카드를 눌렀을 때). 내가 볼 수 있는 카드만 |
| GET | `/prayer/cards/mine` | 내가 올린 카드. 기도 수와 (스냅샷이 NAMED/선택인 경우) 이름 공개한 기도자 목록 |
| POST | `/prayer/cards/draw` | 한 장 뽑기. 내 교회·노출 중·만료 전·내가 쓴 것 제외·내가 이미 기도한 것 제외·내가 차단한 사람 제외. 기도 수가 적은 카드에 가중치. 하루 뽑기 횟수 초과 시 거절 |
| POST | `/prayer/cards/{id}/respond` | "기도했어요". 본문 `{showName?, cheerCode?}`. 카드 스냅샷이 허용할 때만 `showName` 이 유효. 같은 카드 중복 기도는 무시(멱등) |
| PUT | `/prayer/cards/{id}/answered` | 작성자가 "응답됐어요" 표시 |
| DELETE | `/prayer/cards/{id}` | 작성자 본인이 삭제 |
| POST | `/prayer/cards/{id}/report` | 신고. 같은 사람 중복은 무시. 설정 임계치 도달 시 `HIDDEN` 으로 자동 전환 |

`draw` 응답에는 카드 id, 내용, 표시 이름(NAME 일 때만), 그리고 기도자가 받게 될 공개 정책 문구(`responderNotice`)를 포함한다. 앱은 이 문구를 기도 버튼 위에 그대로 보여 준다.

## 관리자 카드 관리

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/prayer/admin/reports` | 신고된 카드 목록. 작성자 정보는 카드 스냅샷 `admin_sees_anon` 이 `REPORTED_ONLY`/`ALWAYS` 일 때만 |
| PUT | `/prayer/admin/cards/{id}/hide` · `/restore` | 숨김/복원 (신고 처리 상태도 함께 갱신) |
| GET | `/prayer/admin/pending` | 사전 검수 대기 목록 |
| PUT | `/prayer/admin/cards/{id}/approve` · `/reject` | 승인/반려 |
| GET | `/prayer/admin/stats` | 카드 수, 기도 횟수, 기도받지 못한 카드 수, 신고 수. 집계만 내려 주고 개인 식별 정보는 없음 |

## 트리

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/prayer/tree` | 이벤트가 켜져 있고 기간 안일 때만. `{cards:[{id,slot,lit}], litCount, totalCount, progress, minCards}`. 카드 수가 `minCards` 미만이면 `progress` 는 null |

## 푸시 (설정 `summary_push = true` 일 때)

- 하루 한 번 스케줄러가 어제 기도를 받은 카드의 작성자에게 요약 푸시를 보낸다 ("어제 N명이 기도해 주셨어요"). 개별 기도마다 푸시하지 않는다.
- 이벤트 시작 안내 푸시는 이번 범위에 넣지 않았다. 관리자 화면의 기존 "알림 보내기"(교회 전체 대상)로 보낸다.
- 요약 푸시를 누르면 앱의 기도 쪽지 홈(`/ecclesia/prayer`)으로 이동한다.
- 스케줄러(`@Scheduled`, 매일 09:00 서버 시간)는 서버가 한 대라는 전제다. 여러 대로 늘리면 중복 발송되므로 그때 분산 잠금을 넣는다.

## 구현 메모

- 성도용 API 의 참여 조건: 기능이 켜져 있고, 교회 가입이 승인된 성도(`ecclesiaStatus = APL`). 설정 `who_can_write = ALL_MEMBERS` 이면 가입 요청 중인 성도도 포함한다.
- 한 사람이 동시에 올려 둘 수 있는 카드는 5개 (도배 방지). 뽑기는 기도를 가장 덜 받은 카드 중에서 무작위.
- 쿼리 검증: `PrayerRepositoryQueryTest` 가 DB 없이 `@Query` 문법과 파생 쿼리의 속성 이름을 확인한다.

## 구현 시 주의

1. 카드 생성 시 정책 스냅샷 복사를 서비스 한 곳에서만 한다 (테스트로 고정).
2. `draw` 의 제외 조건과 하루 횟수 제한은 단위 테스트로 검증한다.
3. 응답 DTO 에 `authorUid` 필드가 없음을 테스트로 보장한다 (익명성 회귀 방지).
4. 설정 변경 이력은 변경이 없으면 기록하지 않는다.
5. 운영 DDL 은 PR 머지 후 `to_regclass` 로 존재 여부를 확인하고 `psql -1` 로 직접 적용한다 (`CREATE ... IF NOT EXISTS` 만 포함).
