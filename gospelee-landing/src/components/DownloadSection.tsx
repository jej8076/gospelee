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
              App Store와 Google Play에서 바로 설치할 수 있어요.<br />
              Android는 APK로 직접 설치할 수도 있습니다.
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
                    <span className={styles.versionChip}>직접 설치</span>
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
                  <span>APK 직접 다운로드</span>
                </a>
              </div>

              <div className={styles.storeStatusGroup}>
                <a href="https://apps.apple.com/app/id6766432856" className={styles.storeLink} target="_blank" rel="noopener noreferrer">
                  <span className={styles.storeIcon}>🍎</span>
                  <span className={styles.storeText}>App Store</span>
                  <span className={styles.openBadge}>설치하기</span>
                </a>
                <a href="https://play.google.com/store/apps/details?id=org.podo" className={styles.storeLink} target="_blank" rel="noopener noreferrer">
                  <span className={styles.storeIcon}>▶️</span>
                  <span className={styles.storeText}>Google Play</span>
                  <span className={styles.openBadge}>설치하기</span>
                </a>
              </div>
            </div>

            {/* QR Code Scan Card for Desktop */}
            <div className={styles.qrCard}>
              <div className={styles.qrVisual}>
                <svg viewBox="-2 -2 33 33" width="130" height="130" role="img" aria-label="Podo 설치 페이지로 연결되는 QR 코드" shapeRendering="crispEdges">
                  <rect x="-2" y="-2" width="33" height="33" fill="#ffffff"/>
                  <path d="M0 0h7v1h-7zM10 0h1v1h-1zM15 0h2v1h-2zM19 0h2v1h-2zM22 0h7v1h-7zM0 1h1v1h-1zM6 1h1v1h-1zM11 1h2v1h-2zM17 1h2v1h-2zM20 1h1v1h-1zM22 1h1v1h-1zM28 1h1v1h-1zM0 2h1v1h-1zM2 2h3v1h-3zM6 2h1v1h-1zM8 2h1v1h-1zM10 2h1v1h-1zM12 2h1v1h-1zM14 2h1v1h-1zM16 2h1v1h-1zM18 2h2v1h-2zM22 2h1v1h-1zM24 2h3v1h-3zM28 2h1v1h-1zM0 3h1v1h-1zM2 3h3v1h-3zM6 3h1v1h-1zM8 3h1v1h-1zM12 3h1v1h-1zM16 3h4v1h-4zM22 3h1v1h-1zM24 3h3v1h-3zM28 3h1v1h-1zM0 4h1v1h-1zM2 4h3v1h-3zM6 4h1v1h-1zM8 4h1v1h-1zM10 4h1v1h-1zM13 4h3v1h-3zM17 4h4v1h-4zM22 4h1v1h-1zM24 4h3v1h-3zM28 4h1v1h-1zM0 5h1v1h-1zM6 5h1v1h-1zM8 5h2v1h-2zM11 5h7v1h-7zM22 5h1v1h-1zM28 5h1v1h-1zM0 6h7v1h-7zM8 6h1v1h-1zM10 6h1v1h-1zM12 6h1v1h-1zM14 6h1v1h-1zM16 6h1v1h-1zM18 6h1v1h-1zM20 6h1v1h-1zM22 6h7v1h-7zM8 7h1v1h-1zM12 7h2v1h-2zM15 7h1v1h-1zM19 7h2v1h-2zM0 8h1v1h-1zM2 8h5v1h-5zM11 8h2v1h-2zM14 8h2v1h-2zM17 8h3v1h-3zM22 8h5v1h-5zM0 9h1v1h-1zM3 9h2v1h-2zM9 9h1v1h-1zM15 9h2v1h-2zM18 9h2v1h-2zM21 9h4v1h-4zM28 9h1v1h-1zM1 10h2v1h-2zM4 10h9v1h-9zM17 10h2v1h-2zM20 10h3v1h-3zM24 10h1v1h-1zM0 11h1v1h-1zM7 11h5v1h-5zM14 11h1v1h-1zM16 11h1v1h-1zM19 11h2v1h-2zM23 11h3v1h-3zM27 11h1v1h-1zM0 12h7v1h-7zM8 12h1v1h-1zM17 12h1v1h-1zM19 12h1v1h-1zM25 12h2v1h-2zM0 13h1v1h-1zM3 13h3v1h-3zM12 13h4v1h-4zM18 13h7v1h-7zM28 13h1v1h-1zM5 14h3v1h-3zM9 14h1v1h-1zM12 14h4v1h-4zM22 14h1v1h-1zM25 14h2v1h-2zM0 15h6v1h-6zM7 15h1v1h-1zM13 15h1v1h-1zM15 15h2v1h-2zM18 15h1v1h-1zM20 15h4v1h-4zM27 15h1v1h-1zM0 16h3v1h-3zM5 16h2v1h-2zM9 16h1v1h-1zM11 16h2v1h-2zM14 16h2v1h-2zM17 16h1v1h-1zM25 16h2v1h-2zM0 17h1v1h-1zM2 17h1v1h-1zM5 17h1v1h-1zM7 17h4v1h-4zM12 17h1v1h-1zM16 17h9v1h-9zM26 17h1v1h-1zM28 17h1v1h-1zM0 18h1v1h-1zM2 18h3v1h-3zM6 18h3v1h-3zM10 18h1v1h-1zM12 18h1v1h-1zM15 18h3v1h-3zM20 18h1v1h-1zM23 18h2v1h-2zM26 18h1v1h-1zM0 19h1v1h-1zM2 19h1v1h-1zM4 19h1v1h-1zM8 19h2v1h-2zM11 19h1v1h-1zM14 19h2v1h-2zM19 19h2v1h-2zM22 19h1v1h-1zM24 19h1v1h-1zM27 19h1v1h-1zM0 20h1v1h-1zM4 20h3v1h-3zM8 20h1v1h-1zM10 20h1v1h-1zM12 20h1v1h-1zM15 20h1v1h-1zM17 20h1v1h-1zM19 20h6v1h-6zM26 20h3v1h-3zM8 21h1v1h-1zM10 21h1v1h-1zM12 21h3v1h-3zM16 21h1v1h-1zM20 21h1v1h-1zM24 21h5v1h-5zM0 22h7v1h-7zM9 22h3v1h-3zM13 22h5v1h-5zM19 22h2v1h-2zM22 22h1v1h-1zM24 22h3v1h-3zM0 23h1v1h-1zM6 23h1v1h-1zM8 23h1v1h-1zM11 23h1v1h-1zM13 23h1v1h-1zM16 23h1v1h-1zM19 23h2v1h-2zM24 23h1v1h-1zM0 24h1v1h-1zM2 24h3v1h-3zM6 24h1v1h-1zM8 24h1v1h-1zM10 24h2v1h-2zM14 24h1v1h-1zM17 24h1v1h-1zM20 24h5v1h-5zM26 24h2v1h-2zM0 25h1v1h-1zM2 25h3v1h-3zM6 25h1v1h-1zM8 25h1v1h-1zM10 25h4v1h-4zM16 25h3v1h-3zM21 25h1v1h-1zM25 25h4v1h-4zM0 26h1v1h-1zM2 26h3v1h-3zM6 26h1v1h-1zM8 26h2v1h-2zM15 26h1v1h-1zM17 26h1v1h-1zM21 26h7v1h-7zM0 27h1v1h-1zM6 27h1v1h-1zM10 27h7v1h-7zM18 27h3v1h-3zM22 27h1v1h-1zM24 27h2v1h-2zM27 27h1v1h-1zM0 28h7v1h-7zM8 28h1v1h-1zM10 28h1v1h-1zM13 28h1v1h-1zM17 28h1v1h-1zM19 28h1v1h-1zM21 28h3v1h-3zM26 28h1v1h-1z" fill="#1e293b"/>
                </svg>
              </div>
              <div className={styles.qrInfo}>
                <span className={styles.qrLabel}>스마트폰 카메라로 스캔</span>
                <span className={styles.qrSub}>PC 화면에서 폰으로 비추면 설치 페이지가 열려요.</span>
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

