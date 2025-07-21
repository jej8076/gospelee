# Gospelee Landing Page

Gospelee 서비스의 랜딩페이지입니다.

## 기술 스택

- Next.js 14
- TypeScript
- CSS Modules
- React

## 개발 환경 설정

1. 의존성 설치:
```bash
npm install
```

2. 개발 서버 실행:
```bash
npm run dev
```

3. 빌드:
```bash
npm run build
```

## 프로젝트 구조

```
src/
├── app/
│   ├── globals.css
│   ├── layout.tsx
│   ├── page.tsx
│   └── developer/
│       ├── page.tsx
│       └── developer.module.css
├── components/
│   ├── Header.tsx
│   ├── Header.module.css
│   ├── HeroSection.tsx
│   ├── HeroSection.module.css
│   ├── FeaturesSection.tsx
│   ├── FeaturesSection.module.css
│   ├── DownloadSection.tsx
│   ├── DownloadSection.module.css
│   ├── Footer.tsx
│   └── Footer.module.css
└── public/
    └── images/
        └── logo/
            └── logo_oog.svg
```

## 특징

- 반응형 디자인 (모바일/데스크톱 지원)
- 쿠팡이츠 스타일의 모던한 UI
- SEO 최적화
- 정적 사이트 생성 (Static Export)
