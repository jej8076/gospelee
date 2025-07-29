'use client';

import {useState, useEffect} from 'react';
import Image from 'next/image';
import styles from './HeroSection.module.css';
import Link from "next/link";

const HeroSection = () => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [fontLoaded, setFontLoaded] = useState(false);
  const words = ['our', 'my'];

  const getDelay = (index: number) => {
    if (index === 0) return 2000;
    if (index === 1) return 1000;
    return 2000;
  };

  useEffect(() => {
    // 폰트 로딩 확인
    const checkFontLoaded = async () => {
      try {
        await document.fonts.load('400 16px Pretendard');
        await document.fonts.load('600 16px Pretendard');
        await document.fonts.load('700 16px Pretendard');
        setFontLoaded(true);
      } catch (error) {
        // 폰트 로딩 실패시에도 3초 후 표시
        setTimeout(() => setFontLoaded(true), 3000);
      }
    };

    checkFontLoaded();
  }, []);

  useEffect(() => {
    const delay = getDelay(currentIndex);

    const timer = setTimeout(() => {
      setCurrentIndex(prev => (prev + 1) % words.length);
    }, delay);

    return () => clearTimeout(timer);
  }, [currentIndex]);

  return (
      <section className={`${styles.hero} ${fontLoaded ? styles.fontLoaded : styles.fontLoading}`}>
        <div className="container">
          <div className={styles.heroContent}>
            <div className={styles.textContent}>
              <h1 className={styles.title}>
                Oh{' '}
                <span className={styles.wordContainer}>
                  {words.map((word, index) => (
                      <span
                          key={word}
                          className={`${styles.animatedWord} ${
                              index === currentIndex ? styles.active : ''
                          }`}
                          style={{
                            transform: `translateY(${(index - currentIndex) * 100}%)`
                          }}
                      >
                      {word}
                    </span>
                  ))}
                </span>{' '}
                God
              </h1>
              <p className={styles.subtitle}>
                우리는 교회, 교회는 우리<br/>
              </p>
              <div className={styles.buttonGroup}>
                <Link href="/#download">
                  <button className={styles.primaryButton}>
                    앱 다운로드
                  </button>
                </Link>
                <button className={styles.secondaryButton}>
                  더 알아보기
                </button>
              </div>
            </div>
            <div className={styles.imageContent}>
              <div className={styles.phoneContainer}>
                <div className={styles.phone}>
                  <div className={styles.phoneScreen}>
                    <div className={styles.appPreview}>
                      <div className={styles.previewHeader}>
                        <div className={styles.previewTitle}>
                          <Image
                              src="/images/logo/logo_oog.svg"
                              alt="OOG Logo"
                              width={60}
                              height={30}
                              className={styles.logoImage}
                          />
                        </div>
                      </div>
                      <div className={styles.previewContent}>
                        <div className={styles.verseCard}>
                          <p className={styles.verseText}>
                            "그의 안에서 건물마다<br/> 서로 연결되어 주 안에서 성전이 되어 가고"
                          </p>
                          <span className={styles.verseRef}>에베소서 2:21</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
  );
};

export default HeroSection;
