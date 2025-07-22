'use client';

import Image from 'next/image';
import { useState } from 'react';
import styles from './Header.module.css';

const Header = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  return (
    <header className={styles.header}>
      <div className="container">
        <div className={styles.headerContent}>
          <div className={styles.logo}>
            <a href="/">
              <Image
                src="/images/logo/logo_oog.svg"
                alt="Gospelee"
                width={120}
                height={40}
                priority
              />
            </a>
          </div>
          
          <nav className={`${styles.nav} ${isMenuOpen ? styles.navOpen : ''}`}>
            <ul className={styles.navList}>
              <li className={styles.navItem}>
                <a href="/developer" className={styles.navLink}>
                  개발자 이야기
                </a>
              </li>
              <li className={styles.navItem}>
                <a href="/#download" className={styles.navLink}>
                  앱 다운로드
                </a>
              </li>
            </ul>
          </nav>

          <button 
            className={styles.menuButton}
            onClick={toggleMenu}
            aria-label="메뉴 열기"
          >
            <span className={styles.menuIcon}></span>
            <span className={styles.menuIcon}></span>
            <span className={styles.menuIcon}></span>
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;
