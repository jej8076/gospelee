'use client';

import Image from 'next/image';
import styles from './HeroSection.module.css';

const HeroSection = () => {
  return (
      <section className={styles.hero}>
        <div className="container">
          <div className={styles.heroContent}>
            <div className={styles.textContent}>
              <h1 className={styles.title}>
                함께하는 <br/>
                <span className={styles.highlight}>OOG</span>
              </h1>
              <p className={styles.subtitle}>
                Gospelee와 함께 성경을 더 깊이 이해하고,<br/>
                일상 속에서 말씀을 실천해보세요.
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
