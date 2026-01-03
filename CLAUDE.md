# Gospelee 프로젝트 개발 가이드

## 프로젝트 개요

Gospelee(가스플리)는 교회 관리 플랫폼으로, 교회 등록/승인, 회원 관리, 성경 읽기, 공지사항, 푸시 알림 등의 기능을 제공합니다.

## 프로젝트 구조

```
gospelee/
├── gospelee-api/          # Spring Boot 백엔드 API 서버 (메인)
├── gospelee-common/       # 공통 유틸리티 라이브러리
├── gospelee-socket/       # WebSocket 서버 (게임/실시간 기능)
├── gospelee-admin/        # Next.js 관리자 웹 프론트엔드
└── gospelee-landing/      # Next.js 랜딩 페이지
```

## 기술 스택

### Backend (gospelee-api)
- **Java 17** + **Spring Boot 3.2.3**
- **Spring Security** - JWT 기반 인증 (카카오/애플 소셜 로그인)
- **Spring Data JPA** + **QueryDSL 5.0** - ORM 및 동적 쿼리
- **Spring Data JDBC** - 일부 레포지토리에서 사용
- **MySQL** - 메인 데이터베이스
- **Redis (Lettuce)** - 캐싱 (embedded-redis 개발용)
- **Firebase Admin SDK** - 푸시 알림
- **WebSocket** - 실시간 통신

### Frontend (gospelee-admin)
- **Next.js 15** + **React 19** + **TypeScript**
- **Tailwind CSS** - 스타일링
- **Zustand** - 상태 관리
- **Framer Motion** - 애니메이션
- **Turbopack** - 개발 서버

### Frontend (gospelee-landing)
- **Next.js 14** + **React 18** + **TypeScript**

## 빌드 및 실행

```bash
# 백엔드 빌드
./gradlew :gospelee-api:bootJar

# 프론트엔드 개발 서버
cd gospelee-admin && npm run dev

# 랜딩 페이지 개발 서버
cd gospelee-landing && npm run dev
```

---

## 코딩 컨벤션

### Java/Spring 백엔드

#### 패키지 구조
```
com.gospelee.api/
├── auth/jwt/              # JWT 인증 관련
├── config/                # 설정 클래스
├── controller/            # REST 컨트롤러
├── dto/                   # 데이터 전송 객체
│   ├── account/
│   ├── common/            # ResponseDTO, DataResponseDTO
│   └── {domain}/
├── entity/                # JPA 엔티티
│   └── common/            # EditInfomation (BaseEntity)
├── enums/                 # 열거형
├── exception/             # 커스텀 예외
├── properties/            # 설정 프로퍼티
├── repository/
│   ├── jpa/{domain}/      # JPA 레포지토리
│   └── jdbc/{domain}/     # JDBC 레포지토리
├── service/               # 서비스 인터페이스/구현체
└── utils/                 # 유틸리티 클래스
```

#### 클래스 네이밍
- **Controller**: `{Domain}Controller` (예: `AccountController`)
- **Service Interface**: `{Domain}Service` (예: `AccountService`)
- **Service Implementation**: `{Domain}ServiceImpl` (예: `AccountServiceImpl`)
- **Repository**: `{Domain}Repository` 또는 `{Domain}JpaRepository`/`{Domain}JdbcRepository`
- **Entity**: 도메인명 그대로 (예: `Account`, `Ecclesia`)
- **DTO**: `{Domain}DTO`, `{Domain}RequestDTO`, `{Domain}ResponseDTO`

#### 컨트롤러 패턴
```java
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/{domain}")
public class DomainController {

  private final DomainService domainService;

  // 조회 API는 @PostMapping 사용 (프로젝트 관례)
  @PostMapping
  public ResponseEntity<Object> getItem(@AuthenticationPrincipal AccountAuthDTO account) {
    return ResponseEntity.ok(
        DataResponseDTO.of("100", "성공", result)
    );
  }

  // 수정은 @PatchMapping 사용
  @PatchMapping("/status")
  public ResponseEntity<Object> updateStatus(@RequestBody UpdateDTO dto) {
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
```

#### 응답 형식
```java
// 성공 응답 (데이터 포함)
DataResponseDTO.of("100", "성공", data)

// 성공 응답 (데이터 없음)
ResponseDTO.of("100", "성공")

// 에러 응답은 ErrorResponseType enum 사용
ErrorResponseType.AUTH_103  // code: "AUTH-103", message: "토큰 유효성 검사 실패"
```

#### Entity 패턴
```java
@Entity
@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends EditInfomation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private long uid;

  // Setter 대신 도메인 메서드 사용
  public void changeRole(RoleType role) {
    this.role = role;
  }

  @Builder
  public Account(...) { }
}
```

#### 서비스 패턴
```java
@Slf4j
@Service
@RequiredArgsConstructor  // 또는 생성자 주입
public class DomainServiceImpl implements DomainService {

  // 인증된 사용자 가져오기
  AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();

  // 권한 체크
  if (!RoleType.ADMIN.equals(account.getRole())) {
    throw new AccessDeniedException("접근할 권한이 없습니다.");
  }
}
```

#### Enum 패턴
```java
public enum ErrorResponseType {
  AUTH_103("AUTH-103", "토큰 유효성 검사 실패"),
  ECCL_101("ECCL-101", "교회 정보가 없음");

  private final String code;
  private final String message;

  public String code() { return code; }
  public String message() { return message; }
}
```

#### 커스텀 예외
- 도메인별 예외 클래스 생성: `AccountNotFoundException`, `EcclesiaException`
- `ApiControllerAdvice`에서 전역 예외 처리

#### 로깅 패턴
```java
log.info("[PUSHTOKEN ] update_success accountUid:{} pushToken:{}", uid, token);
log.error("[ACCOUNT   ] not_found accountUid:{}", uid);
```

### TypeScript/React 프론트엔드 (gospelee-admin)

#### 디렉토리 구조
```
src/
├── app/                   # Next.js App Router 페이지
│   ├── {route}/page.tsx
│   └── layout.tsx
├── components/            # 재사용 컴포넌트
│   ├── modal/
│   ├── manage/
│   └── {domain}/
├── enums/                 # 열거형 상수
├── hooks/                 # 커스텀 훅
│   ├── useApiClient.ts
│   └── useMenuList.ts
├── types/                 # TypeScript 타입 정의
└── utils/                 # 유틸리티 함수
```

#### 컴포넌트 패턴
```tsx
'use client'

import useAuth from "~/lib/auth/check-auth";

export default function PageComponent() {
  useAuth();  // 인증 체크

  return (
    <div>...</div>
  );
}
```

#### API 클라이언트
```typescript
// lib/api-client.ts
export const apiFetch = async (endpoint: string, options?: RequestInit, timeout = 3000) => {
  // 타임아웃 처리 포함
  // X-App-Identifier 헤더 자동 추가
};

// hooks/useApiClient.ts
export const useApiClient = () => {
  const { callApi } = useApiClient();
  // 401 시 로그인 페이지 리다이렉트
  // 에러 핸들링 통합
};
```

#### 파일 네이밍
- 컴포넌트: `kebab-case.tsx` (예: `modal-buttons.tsx`)
- 훅: `use{Name}.ts` (예: `useApiClient.ts`)
- 페이지: `page.tsx` (Next.js 규칙)

---

## 비즈니스 로직

### 인증 플로우
1. 클라이언트에서 카카오/애플 로그인 후 ID Token 전송
2. `JwtAuthenticationFilter`에서 토큰 검증
3. `SocialJwtProvider`로 OIDC 페이로드 추출
4. `AccountService.saveAndGetAccount()`로 계정 생성/조회
5. `SecurityContextHolder`에 인증 정보 저장
6. 컨트롤러에서 `@AuthenticationPrincipal AccountAuthDTO` 사용

### 권한 체계 (RoleType)
- `LAYMAN` - 일반 평신도
- `TEACHER` - 교사
- `COORDINATOR` - 간사
- `MINISTER` - 전도사
- `LICENSED_MINISTER` - 강도사
- `PASTOR` - 목사(부목사)
- `SENIOR_PASTOR` - 담임목사
- `ADMIN` - 시스템 관리자

### 교회(Ecclesia) 등록 플로우
1. 사용자가 교회 등록 요청 (`EcclesiaStatusType.REQUEST`)
2. 관리자가 승인 (`EcclesiaStatusType.APPROVAL`)
3. 승인 시 요청자 권한이 `SENIOR_PASTOR`로 변경
4. 거절 시 `LAYMAN`으로 유지

### 교회 참여(Join) 플로우
1. 사용자가 교회 검색 후 참여 요청
2. `AccountEcclesiaHistory`에 `JOIN_REQUEST` 상태로 기록
3. 교회 관리자가 승인/거절 결정
4. 승인 시 사용자의 `ecclesiaUid` 설정

### 주요 도메인
- **Account**: 사용자 계정 (소셜 로그인, 푸시 토큰)
- **Ecclesia**: 교회 정보 (마스터 계정, 상태 관리)
- **AccountEcclesiaHistory**: 교회 참여 이력
- **Bible/AccountBibleWrite**: 성경 읽기 기록
- **Announcement**: 공지사항
- **PushNotification**: 푸시 알림 관리
- **Journal**: 일지/묵상 기록

---

## 주요 파일 참조

### 백엔드 핵심 파일
- `gospelee-api/src/main/java/com/gospelee/api/auth/jwt/JwtAuthenticationFilter.java` - JWT 인증 필터
- `gospelee-api/src/main/java/com/gospelee/api/service/AccountServiceImpl.java` - 계정 서비스
- `gospelee-api/src/main/java/com/gospelee/api/service/EcclesiaServiceImpl.java` - 교회 서비스
- `gospelee-api/src/main/java/com/gospelee/api/config/SecurityConfig.java` - 보안 설정
- `gospelee-api/src/main/java/com/gospelee/api/exception/ApiControllerAdvice.java` - 전역 예외 처리

### 프론트엔드 핵심 파일
- `gospelee-admin/lib/api-client.ts` - API 클라이언트
- `gospelee-admin/src/hooks/useApiClient.ts` - API 호출 훅
- `gospelee-admin/src/app/layout.tsx` - 루트 레이아웃

---

## 개발 시 주의사항

1. **인증**: 모든 API는 기본적으로 JWT 인증 필요 (`AuthProperties.excludePaths` 참조)
2. **권한 체크**: 서비스 레이어에서 `AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow()` 사용
3. **응답 형식**: `DataResponseDTO.of("100", "성공", data)` 통일
4. **트랜잭션**: 서비스 메서드에 `@Transactional` 명시
5. **로깅**: `[도메인명   ]` 형식으로 로그 prefix 통일
6. **Entity 수정**: Setter 대신 `change{Field}()` 도메인 메서드 사용
