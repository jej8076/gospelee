# Gospelee

교회 관리 시스템을 위한 풀스택 웹 애플리케이션

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [시스템 아키텍처](#시스템-아키텍처)
- [프로젝트 구조](#프로젝트-구조)
- [기술 스택](#기술-스택)
- [개발 환경 설정](#개발-환경-설정)
- [코딩 규칙](#코딩-규칙)
- [API 문서](#api-문서)
- [배포](#배포)

## 🎯 프로젝트 개요

OOG 앱을 포함하여 Gospelee 브랜드 전체 시스템을 담당하는 통합 어플리케이션 프로젝트 입니다

### 주요 기능

- **교회 관리**: 교회 정보 관리, 승인 시스템
- **공지사항 관리**: 공지사항 작성, 수정, 파일 첨부
- **브랜드 스토리**: 교회 소개 및 스토리 관리
- **사용자 관리**: 회원 가입 승인, 권한 관리
- **QR 코드 로그인**: 모바일 앱과 연동된 QR 로그인
- **푸시 알림**: 실시간 알림 시스템

## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Landing Page  │    │   Admin Page    │    │   Mobile App    │
│   (Next.js)     │    │   (Next.js)     │    │   (Flutter)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                     │                       │
         └─────────────────────┼───────────────────────┘
                               │
                      ┌─────────────────┐
                      │   API Server    │
                      │ (Spring Boot)   │
                      └─────────────────┘
                               │
                      ┌─────────────────┐
                      │     Database    │
                      │     (MySQL)     │
                      └─────────────────┘
```

## 📁 프로젝트 구조

```
gospelee/
├── gospelee-api/           # Spring Boot API 서버
│   ├── src/main/java/com/gospelee/api/
│   │   ├── controller/     # REST API 컨트롤러
│   │   ├── service/        # 비즈니스 로직
│   │   ├── repository/     # 데이터 액세스 계층
│   │   ├── entity/         # JPA 엔티티
│   │   ├── dto/           # 데이터 전송 객체
│   │   ├── config/        # 설정 클래스
│   │   ├── enums/         # 열거형 상수
│   │   ├── exception/     # 예외 처리
│   │   └── utils/         # 유틸리티 클래스
│   └── src/main/resources/
│       ├── application.yml # 애플리케이션 설정
│       └── data.sql       # 초기 데이터
├── gospelee-admin/         # 관리자 웹 패널
│   ├── src/
│   │   ├── app/           # Next.js App Router 페이지
│   │   ├── components/    # 재사용 가능한 컴포넌트
│   │   ├── hooks/         # 커스텀 React 훅
│   │   ├── types/         # TypeScript 타입 정의
│   │   ├── utils/         # 유틸리티 함수
│   │   └── enums/         # 열거형 상수
│   ├── public/            # 정적 파일
│   └── package.json       # 의존성 관리
├── gospelee-landing/       # 랜딩 페이지
├── gospelee-common/        # 공통 라이브러리
├── dockerfile.api          # API 서버 Docker 설정
├── dockerfile.admin        # 관리자 패널 Docker 설정
└── dockerfile.landing      # 랜딩 페이지 Docker 설정
```

## 🛠️ 기술 스택

### Backend (gospelee-api)

- **Framework**: Spring Boot 3.2.3
- **Language**: Java 17
- **Database**: MySQL (개발, 운영)
- **ORM**: Spring Data JPA + QueryDSL
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle
- **Documentation**: Swagger/OpenAPI

### Frontend (gospelee-admin)

- **Framework**: Next.js 15.4.6 (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **UI Components**: Headless UI, Heroicons
- **State Management**: React Hooks
- **HTTP Client**: Fetch API
- **Build Tool**: npm

### DevOps

- **Develop Environment**: Teleport
- **Containerization**: Docker
- **Database**: Mysql
- **Version Control**: Git

## 🚀 개발 환경 설정

### 필수 요구사항

- Java 17+
- Node.js 18+
- npm 또는 yarn
- Docker (선택사항)

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd gospelee
```

### 2. API 서버 실행

```bash
# Gradle을 사용한 실행
./gradlew :gospelee-api:bootRun

# 또는 IDE에서 ApiApplication.java 실행
```

### 3. 관리자 패널 실행

```bash
cd gospelee-admin
npm install
npm run dev
```

### 4. 접속 정보

- **API 서버**: http://localhost:8080
- **관리자 패널**: http://localhost:3000
- **API 문서**: http://localhost:8080/swagger-ui.html

## 📝 코딩 규칙

### 데이터베이스 규칙

1. **테이블과 컬럼명은 무조건 소문자로 작성**
   ```sql
   -- 올바른 예
   CREATE TABLE announcement (
       id BIGINT PRIMARY KEY,
       organization_type VARCHAR(50)
   );
   ```

2. **두 개 이상의 단어가 있을 경우 _(언더바) 사용**
   ```sql
   -- 올바른 예
   organization_type, push_notification_send_yn, file_detail_id
   ```

### Entity 규칙

1. **컬럼명에 _(underbar)가 있을 경우 Entity field는 camelCase로 작성**
   ```java
   @Entity
   public class Announcement {
       @Column(name = "organization_type")
       private String organizationType;
       
       @Column(name = "push_notification_send_yn")
       private String pushNotificationSendYn;
   }
   ```

### Frontend 규칙

1. **컴포넌트명은 PascalCase**
   ```typescript
   // 올바른 예
   export default function AnnouncementList() { }
   ```

2. **파일명은 kebab-case 또는 camelCase**
   ```
   announcement-list.tsx
   announcementList.tsx
   ```

3. **Hook 의존성 배열 규칙**
   ```typescript
   // useCallback과 useEffect에서 의존성 배열 명시
   const loadData = useCallback(async () => {
     // 로직
   }, [callApi, id]);
   
   useEffect(() => {
     loadData();
   }, [loadData]);
   ```

### API 규칙

1. **REST API 엔드포인트는 kebab-case**
   ```
   GET /api/announcements
   POST /api/announcements
   PUT /api/announcements/{id}
   DELETE /api/announcements/{id}
   ```

2. **DTO 클래스는 PascalCase + DTO 접미사**
   ```java
   public class AnnouncementDTO { }
   public class AccountAuthDTO { }
   ```

## 📚 API 문서

### 주요 엔드포인트

#### 인증

- `POST /api/account/login` - 로그인
- `POST /api/account/qr/make` - QR 코드 생성
- `GET /api/account/qr/check/{code}` - QR 코드 확인

#### 공지사항

- `GET /api/announcements` - 공지사항 목록 조회
- `POST /api/announcements` - 공지사항 생성
- `PUT /api/announcements/{id}` - 공지사항 수정
- `DELETE /api/announcements/{id}` - 공지사항 삭제

#### 파일 관리

- `POST /api/files/upload` - 파일 업로드
- `GET /api/files/{id}` - 파일 다운로드
- `DELETE /api/files/{id}` - 파일 삭제

### 성경 데이터 구조

```
cate = 1: 구약, 2: 신약
book = 성경번호 (예: 40 = 마태복음)
chapter = 장
verse = 절
```

참고: [성경약어표](https://www.bskorea.or.kr/bible/korbib_shortword02.php)

## 🐳 배포

### Docker를 사용한 배포

#### API 서버

```bash
docker build -f dockerfile.api -t gospelee-api .
docker run -p 8080:8080 gospelee-api
```

#### 관리자 패널

```bash
docker build -f dockerfile.admin -t gospelee-admin .
docker run -p 3000:3000 gospelee-admin
```

#### 랜딩 페이지

```bash
docker build -f dockerfile.landing -t gospelee-landing .
docker run -p 3001:3000 gospelee-landing
```

### 환경 변수 설정

#### API 서버 (.env)

```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:mysql://localhost:3306/gospelee
DATABASE_USERNAME=username
DATABASE_PASSWORD=password
JWT_SECRET=your-jwt-secret
```

#### 관리자 패널 (.env.local)

```
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_ENVIRONMENT=production
```

## 🤝 기여 가이드

1. **브랜치 전략**: Git Flow 사용
    - `main`: 운영 브랜치
    - `develop`: 개발 브랜치
    - `feature/*`: 기능 개발 브랜치

2. **커밋 메시지 규칙**:
   ```
   feat: 새로운 기능 추가
   fix: 버그 수정
   docs: 문서 수정
   style: 코드 포맷팅
   refactor: 코드 리팩토링
   test: 테스트 코드
   chore: 빌드 업무 수정
   ```

3. **Pull Request 규칙**:
    - 기능별로 작은 단위로 PR 생성
    - 코드 리뷰 필수
    - 테스트 통과 확인

## 📞 문의

jej8076@gmail.com
프로젝트 관련 문의사항이 있으시면 연락주세요

---
