'use client';

import {useState, useEffect} from 'react';
import Image from 'next/image';
import styles from './HeroSection.module.css';

const HeroSection = () => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const words = ['our', 'my'];

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentIndex(prev => (prev + 1) % words.length);
    }, 2000);

    return () => clearInterval(interval);
  }, []);

  return (
      <section className={styles.hero}>
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
                <button className={styles.primaryButton}>
                  앱 다운로드
                </button>
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
                            "하나님이 세상을 이처럼 사랑하사 독생자를 주셨으니"
                          </p>
                          <span className={styles.verseRef}>요한복음 3:16</span>
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
