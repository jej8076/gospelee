'use client';

import {useEffect, useState} from 'react';
import Image from 'next/image';
import styles from './HeroSection.module.css';
import Link from "next/link";

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
        <div className="container">
          <div className={styles.heroContent}>
            <div className={styles.textContent}>
              <h1 className={styles.title}>
                나는 포도나무요<br/>
                너희는 가지니
              </h1>
              <p className={styles.subtitle}>
                요한복음 15:5<br/>
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
                              src="/images/logo/podo_logo.svg"
                              alt="podo Logo"
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
