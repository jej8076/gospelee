'use client';

import {useEffect, useState} from 'react';
import styles from './HeroSection.module.css';
import Link from "next/link";
import DemoPhone from './DemoPhone';

const HeroSection = () => {
  const [fontLoaded, setFontLoaded] = useState(false);

  useEffect(() => {
    const checkFontLoaded = async () => {
      try {
        await document.fonts.load('400 16px Pretendard');
        await document.fonts.load('600 16px Pretendard');
        await document.fonts.load('700 16px Pretendard');
        setFontLoaded(true);
      } catch (error) {
        setTimeout(() => setFontLoaded(true), 3000);
      }
    };

    checkFontLoaded();
  }, []);

  return (
    <section className={`${styles.hero} ${fontLoaded ? styles.fontLoaded : styles.fontLoading}`}>
      <div className={styles.ambientGlow} />
      <div className="container">
        <div className={styles.heroContent}>
          <div className={styles.textContent}>
            <div className={styles.badgeWrapper}>
              <span className={styles.badgeDot}></span>
              <span className={styles.badgeText}>Solo Engineer · Faith Craft Experiment</span>
            </div>

            <h1 className={styles.title}>
              말씀과 교회를 향한<br />
              <span className={styles.highlightText}>가장 조용한 실험.</span>
            </h1>

            <div className={styles.quoteBlock}>
              <p className={styles.scripture}>
                "나는 포도나무요 너희는 가지니"
              </p>
              <span className={styles.scriptureRef}>요한복음 15:5</span>
            </div>

            <p className={styles.description}>
              광고와 복잡함을 걷어내고 오직 말씀에 깊이 머무는 성경 타이핑 필사부터,
              작은 교회도 부담 없이 전하는 모바일 주보까지.
              1인 개발자가 기독교 문화를 위해 한 땀 한 땀 빚어가는 신앙 도구입니다.
            </p>

            <div className={styles.buttonGroup}>
              <Link href="/#download" className={styles.primaryButton}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="7 10 12 15 17 10"/>
                  <line x1="12" y1="15" x2="12" y2="3"/>
                </svg>
                <span>APK 다운로드</span>
                <span className={styles.buttonVersion}>v1.0.0</span>
              </Link>

              <Link href="/identity" className={styles.secondaryButton}>
                <span>정체성 읽기</span>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="5" y1="12" x2="19" y2="12"/>
                  <polyline points="12 5 19 12 12 19"/>
                </svg>
              </Link>
            </div>
          </div>

          <div className={styles.imageContent}>
            <DemoPhone />
          </div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
