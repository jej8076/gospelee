'use client';

import Image from 'next/image';
import styles from './Footer.module.css';
import Link from "next/link";

const Footer = () => {
  return (
      <footer className={styles.footer}>
        <div className="container">
          <div className={styles.footerContent}>
            <div className={styles.footerTop}>
              <div className={styles.logoSection}>
                <Image
                    src="/images/logo/logo_oog.svg"
                    alt="Gospelee"
                    width={100}
                    height={32}
                />
                <p className={styles.logoDescription}>
                  성경과 함께하는 새로운 경험
                </p>
              </div>

              <div className={styles.linksSection}>
                <div className={styles.linkGroup}>
                  <h4 className={styles.linkTitle}>서비스</h4>
                  <ul className={styles.linkList}>
                    <li><a href="#" className={styles.link}>성경 읽기</a></li>
                    <li><a href="#" className={styles.link}>말씀 묵상</a></li>
                    <li><a href="#" className={styles.link}>기도 노트</a></li>
                    <li><a href="#" className={styles.link}>커뮤니티</a></li>
                  </ul>
                </div>

                <div className={styles.linkGroup}>
                  <h4 className={styles.linkTitle}>지원</h4>
                  <ul className={styles.linkList}>
                    <li><a href="#" className={styles.link}>도움말</a></li>
                    <li><a href="#" className={styles.link}>문의하기</a></li>
                    <li><Link href="/identity" className={styles.link}>브랜드 스토리</Link></li>
                  </ul>
                </div>

                <div className={styles.linkGroup}>
                  <h4 className={styles.linkTitle}>다운로드</h4>
                  <ul className={styles.linkList}>
                    <li><a href="#" className={styles.link}>App Store</a></li>
                    <li><a href="#" className={styles.link}>Google Play</a></li>
                  </ul>
                </div>
              </div>
            </div>

            <div className={styles.footerBottom}>
              <div className={styles.copyright}>
                <p>&copy; 2024 Gospelee. All rights reserved.</p>
              </div>
              <div className={styles.policies}>
                <a href="#" className={styles.policyLink}>개인정보처리방침</a>
                <a href="#" className={styles.policyLink}>이용약관</a>
              </div>
            </div>
          </div>
        </div>
      </footer>
  );
};

export default Footer;
