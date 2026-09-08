'use client';

import styles from './DownloadSection.module.css';

const DownloadSection = () => {
  return (
    <section id="download" className={styles.download}>
      <div className="container">
        <div className={styles.downloadContent}>
          <div className={styles.header}>
            <span className={styles.badge}>RELEASES & DISTRIBUTION</span>
            <h2 className={styles.title}>
              지금 바로, 말씀의 자리로.
            </h2>
            <p className={styles.subtitle}>
              Android 기기에서 즉시 실행 가능한 최신 릴리즈 빌드입니다.<br />
              App Store와 Google Play 공식 출시도 차분히 준비하고 있습니다.
            </p>
          </div>

          <div className={styles.hubGrid}>
            {/* Direct APK Card */}
            <div className={styles.releaseCard}>
              <div className={styles.cardTop}>
                <div className={styles.platformIcon}>
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M17.523 15.3414c-.5511 0-.9993-.4486-.9993-.9997s.4482-.9993.9993-.9993c.551 0 .9993.4482.9993.9993.0001.5511-.4483.9997-.9993.9997m-11.046 0c-.5511 0-.9993-.4486-.9993-.9997s.4482-.9993.9993-.9993c.5511 0 .9993.4482.9993.9993 0 .5511-.4482.9997-.9993.9997m11.4045-6.02l1.9973-3.4592a.416.416 0 00-.1521-.5676.416.416 0 00-.5676.1521l-2.0223 3.503C15.5902 8.411 13.8564 8 12 8s-3.5902.411-5.1367.9497L4.841 5.4467a.4161.4161 0 00-.5677-.1521.4157.4157 0 00-.1521.5676l1.9973 3.4592C2.6889 11.1867.3432 14.6589 0 18.761h24c-.3432-4.1021-2.6889-7.5743-6.1185-9.4396"/>
                  </svg>
                </div>
                <div className={styles.platformMeta}>
                  <div className={styles.platformTitleGroup}>
                    <span className={styles.platformName}>Android Direct Build</span>
                    <span className={styles.versionChip}>v1.0.0 Stable</span>
                  </div>
                  <span className={styles.fileDetail}>app-release.apk · Universal APK</span>
                </div>
              </div>

              <div className={styles.cardAction}>
                <a
                  href="https://landing.podo.kr/download/app-release.apk"
                  download
                  className={styles.downloadBtn}
                >
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                    <polyline points="7 10 12 15 17 10"/>
                    <line x1="12" y1="15" x2="12" y2="3"/>
                  </svg>
                  <span>APK 즉시 다운로드</span>
                </a>
              </div>

              <div className={styles.storeStatusGroup}>
                <div className={styles.storePendingItem}>
                  <span className={styles.storeIcon}>🍎</span>
                  <span className={styles.storeText}>App Store</span>
                  <span className={styles.pendingBadge}>심사 준비 중</span>
                </div>
                <div className={styles.storePendingItem}>
                  <span className={styles.storeIcon}>▶️</span>
                  <span className={styles.storeText}>Google Play</span>
                  <span className={styles.pendingBadge}>출시 준비 중</span>
                </div>
              </div>
            </div>

            {/* QR Code Scan Card for Desktop */}
            <div className={styles.qrCard}>
              <div className={styles.qrVisual}>
                <svg viewBox="0 0 160 160" width="130" height="130" fill="none">
                  {/* Position detection patterns */}
                  <rect x="10" y="10" width="40" height="40" rx="6" stroke="#1e293b" strokeWidth="8"/>
                  <rect x="22" y="22" width="16" height="16" rx="2" fill="#316049"/>
                  
                  <rect x="110" y="10" width="40" height="40" rx="6" stroke="#1e293b" strokeWidth="8"/>
                  <rect x="122" y="22" width="16" height="16" rx="2" fill="#316049"/>
                  
                  <rect x="10" y="110" width="40" height="40" rx="6" stroke="#1e293b" strokeWidth="8"/>
                  <rect x="22" y="122" width="16" height="16" rx="2" fill="#316049"/>

                  {/* Stylized QR bits */}
                  <rect x="62" y="15" width="8" height="8" rx="2" fill="#1e293b"/>
                  <rect x="76" y="15" width="8" height="8" rx="2" fill="#316049"/>
                  <rect x="90" y="15" width="8" height="8" rx="2" fill="#1e293b"/>
                  
                  <rect x="62" y="32" width="16" height="8" rx="2" fill="#316049"/>
                  <rect x="86" y="32" width="8" height="16" rx="2" fill="#1e293b"/>
                  
                  <rect x="15" y="62" width="8" height="16" rx="2" fill="#1e293b"/>
                  <rect x="32" y="62" width="16" height="8" rx="2" fill="#316049"/>
                  <rect x="15" y="86" width="8" height="8" rx="2" fill="#1e293b"/>
                  
                  <rect x="62" y="62" width="36" height="36" rx="8" fill="#edf3f0" stroke="#316049" strokeWidth="2"/>
                  <circle cx="80" cy="80" r="8" fill="#316049"/>
                  
                  <rect x="110" y="62" width="8" height="16" rx="2" fill="#316049"/>
                  <rect x="126" y="70" width="16" height="8" rx="2" fill="#1e293b"/>
                  <rect x="110" y="86" width="24" height="8" rx="2" fill="#1e293b"/>
                  
                  <rect x="62" y="110" width="16" height="8" rx="2" fill="#1e293b"/>
                  <rect x="86" y="110" width="16" height="8" rx="2" fill="#316049"/>
                  <rect x="110" y="110" width="8" height="24" rx="2" fill="#316049"/>
                  <rect x="126" y="126" width="16" height="16" rx="2" fill="#1e293b"/>
                  <rect x="62" y="126" width="8" height="16" rx="2" fill="#316049"/>
                  <rect x="78" y="134" width="16" height="8" rx="2" fill="#1e293b"/>
                </svg>
              </div>
              <div className={styles.qrInfo}>
                <span className={styles.qrLabel}>스마트폰 카메라로 스캔</span>
                <span className={styles.qrSub}>PC 화면에서 폰으로 비추면 즉시 다운로드 링크로 연결됩니다.</span>
              </div>
            </div>
          </div>

          <div className={styles.indieNote}>
            <div className={styles.noteContent}>
              <span className={styles.noteIcon}>💌</span>
              <p className={styles.noteText}>
                Podo는 1인 개발자가 퇴근 후 지속적으로 가꾸어가는 프로젝트입니다.
                사용 중 불편한 점이나 작은 교회의 도입 문의는 편하게{' '}
                <a href="mailto:jej8076@gmail.com" className={styles.noteLink}>jej8076@gmail.com</a>
                으로 보내주세요.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default DownloadSection;

