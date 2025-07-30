'use client';

import styles from './DownloadSection.module.css';

const DownloadSection = () => {
  return (
      <section id="download" className={styles.download}>
        <div className="container">
          <div className={styles.downloadContent}>
            <div className={styles.textContent}>
              <h2 className={styles.title}>
                시작하기
              </h2>
              <p className={styles.subtitle}>
                OOG 앱을 다운로드하고<br/>
                말씀을 다양하게 묵상해 보세요
              </p>
              <a href="https://landing.oog.kr/download/app-release.apk" download>
                <div className={styles.storeButtons}>
                  <button className={styles.storeButton}>
                    <div className={styles.storeIcon}>📱</div>
                    <div className={styles.storeText}>
                      <span className={styles.storeLabel}>Download on the</span>
                      <span className={styles.storeName}>App Store</span>
                    </div>
                  </button>
                  <button className={styles.storeButton}>
                    <div className={styles.storeIcon}>🤖</div>
                    <div className={styles.storeText}>
                      <span className={styles.storeLabel}>Get it on</span>
                      <span className={styles.storeName}>Google Play</span>
                    </div>
                  </button>
                </div>
              </a>
            </div>
            <div className={styles.imageContent}>
              <div className={styles.phonesContainer}>
                <div className={styles.phoneLeft}>
                  <div className={styles.phoneScreen}>
                    <div className={styles.appContent}>
                      <div className={styles.appHeader}>성경 읽기</div>
                      <div className={styles.verseList}>
                        <div className={styles.verseItem}>창세기 1:1</div>
                        <div className={styles.verseItem}>시편 23:1</div>
                        <div className={styles.verseItem}>요한복음 3:16</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div className={styles.phoneRight}>
                  <div className={styles.phoneScreen}>
                    <div className={styles.appContent}>
                      <div className={styles.appHeader}>오늘의 말씀</div>
                      <div className={styles.todayVerse}>
                        <p>"여호와는 나의 목자시니 내게 부족함이 없으리로다"</p>
                        <span>시편 23:1</span>
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

export default DownloadSection;
