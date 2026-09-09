'use client';

import { Suspense, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import styles from './JoinLanding.module.css';

function JoinContent() {
  const searchParams = useSearchParams();
  const code = searchParams.get('code') || '';

  const deepLinkUrl = code
    ? `podo://bible-reading/join?code=${encodeURIComponent(code)}`
    : 'podo://bible-reading';

  useEffect(() => {
    // 모바일 기기에서 자동 앱 오픈 시도
    if (code && typeof window !== 'undefined') {
      const isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);
      if (isMobile) {
        const timer = setTimeout(() => {
          window.location.href = deepLinkUrl;
        }, 500);
        return () => clearTimeout(timer);
      }
    }
  }, [code, deepLinkUrl]);

  return (
    <div className={styles.card}>
      <div className={styles.badge}>
        <span>📖</span> 함께하는 성경 통독
      </div>
      <h1 className={styles.title}>말씀의 동행에 초대합니다</h1>
      <p className={styles.desc}>
        지체와 함께 목표를 나누고 매일 말씀을 통독해보세요.<br />
        포도 앱에서 즉시 목표에 참여할 수 있습니다.
      </p>

      {code && (
        <div className={styles.codeBox}>
          초대 코드: <strong>{code}</strong>
        </div>
      )}

      <div className={styles.btnGroup}>
        <a href={deepLinkUrl} className={styles.openBtn}>
          <span>포도 앱에서 열기</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M5 12h14M12 5l7 7-7 7"/>
          </svg>
        </a>
        <a href="/#download" className={styles.downloadBtn}>
          <span>앱 다운로드 안내</span>
        </a>
      </div>
    </div>
  );
}

export default function BibleReadingJoinPage() {
  return (
    <main className={styles.container}>
      <Suspense fallback={<div className={styles.card}><p>초대 정보를 불러오는 중...</p></div>}>
        <JoinContent />
      </Suspense>
    </main>
  );
}
