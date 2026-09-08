'use client';

import Image from 'next/image';
import Link from 'next/link';
import styles from './Footer.module.css';

const Footer = () => {
  return (
    <footer className={styles.footer}>
      <div className="container">
        <div className={styles.footerContent}>
          <div className={styles.footerTop}>
            <div className={styles.brandCol}>
              <Link href="/" className={styles.logoRow}>
                <Image
                  src="/images/logo/podo_logo.svg"
                  alt="Podo Logo"
                  width={28}
                  height={20}
                  className={styles.logoImage}
                />
                <span className={styles.brandTitle}>Podo</span>
              </Link>
              <p className={styles.brandDesc}>
                소란스러운 일상 속 말씀에 머무르는 자리.<br />
                1인 개발자가 기독교 문화를 위해 빚어가는 신앙 도구입니다.
              </p>
              <span className={styles.verseLabel}>요한복음 15:5 — "나는 포도나무요 너희는 가지니"</span>
            </div>

            <div className={styles.linksGrid}>
              <div className={styles.linkGroup}>
                <h4 className={styles.groupTitle}>제품</h4>
                <ul className={styles.linkList}>
                  <li><Link href="/#features" className={styles.link}>말씀 필사</Link></li>
                  <li><Link href="/#features" className={styles.link}>모바일 주보</Link></li>
                  <li><Link href="/#download" className={styles.link}>APK 다운로드</Link></li>
                </ul>
              </div>

              <div className={styles.linkGroup}>
                <h4 className={styles.groupTitle}>프로젝트</h4>
                <ul className={styles.linkList}>
                  <li><Link href="/identity" className={styles.link}>정체성 & 스토리</Link></li>
                  <li><a href="mailto:jej8076@gmail.com" className={styles.link}>개발자에게 편지하기</a></li>
                </ul>
              </div>

              <div className={styles.linkGroup}>
                <h4 className={styles.groupTitle}>지원</h4>
                <ul className={styles.linkList}>
                  <li><Link href="/support" className={styles.link}>고객지원</Link></li>
                  <li><Link href="/privacy-policy" className={styles.link}>개인정보처리방침</Link></li>
                </ul>
              </div>
            </div>
          </div>

          <div className={styles.footerBottom}>
            <p className={styles.copyright}>
              &copy; {new Date().getFullYear()} Podo. Built independently with care & prayer.
            </p>
            <div className={styles.bottomLinks}>
              <Link href="/privacy-policy" className={styles.bottomLink}>개인정보처리방침</Link>
              <span className={styles.dotDivider}>·</span>
              <Link href="/support" className={styles.bottomLink}>고객지원</Link>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;

