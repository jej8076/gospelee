# GEMINI.md (gospelee)

이 파일은 `gospelee` 프로젝트의 백엔드(api), 어드민(admin), 랜딩 페이지(landing) 개발 시 준수해야 하는 핵심 지침을 담고 있습니다.

## 1. 프로젝트 구조 및 기술 스택
- **Multi-Module Gradle**: `gospelee-api`, `gospelee-admin`, `gospelee-landing`, `gospelee-common`
- **Backend (api)**: Spring Boot 3.x, Java 17, JPA (Hibernate), PostgreSQL, Redis
- **Frontend (admin/landing)**: Next.js (App Router), TypeScript, TailwindCSS

## 2. 백엔드 (gospelee-api) 개발 원칙

### 2.1. 데이터 영속성 및 ORM
- **Framework**: Hibernate JPA를 사용합니다.
- **Entity 관리**:
  - Lombok의 `@Data` 사용을 지양하고, `@Getter`, `@Setter`를 명시적으로 사용합니다.
  - 데이터 수정 시 단순 Setter 호출 대신 의미 있는 이름의 메서드(예: `updateStatus`, `changeProfile`)를 엔티티 내에 정의하여 사용합니다.
- **SQL 가이드**:
  - `Bible.sql` 등 대용량 데이터 파일 작성 시, 문자열 내 작은따옴표 이스케이프는 SQL 표준인 `''` (두 번 연속)을 사용합니다. (`\'` 사용 금지)

### 2.2. 데이터 전송 (DTO)
- **Map 사용 금지**: API 응답 및 요청 데이터 전송 시 반드시 DTO 클래스를 생성합니다.
- **DTO 구조**: 계층 구조가 필요한 경우 중첩 클래스(Nested Class) 대신 패키지 분리를 고려합니다.

### 2.3. 로컬 환경 설정
- **application-local.yml**: 로컬 개발 시 DB SSL 모드 및 인증서 경로가 본인의 환경과 맞는지 항상 확인합니다. (기본적으로 `sslmode=disable` 권장)

## 3. 프론트엔드 (gospelee-admin / landing) 개발 원칙

### 3.1. 기술 스택 및 스타일
- **Framework**: Next.js App Router 방식을 따릅니다.
- **Styling**: TailwindCSS를 기본으로 사용하며, 일관된 디자인 시스템을 유지합니다.
- **State Management**: React Hooks를 우선 사용하고, 복잡한 상태는 필요 시 Context API 또는 관련 도구를 활용합니다.

### 3.2. 컴포넌트 설계
- UI 컴포넌트는 `src/components` 하위에 기능별로 분리하여 재사용성을 높입니다.
- 비즈니스 로직과 UI 렌더링 로직을 분리하기 위해 커스텀 훅(`src/hooks`)을 적극 활용합니다.

---
*이 지침은 프로젝트 진행 과정에서 지속적으로 업데이트됩니다.*
