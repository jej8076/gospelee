'use client';

import Image from 'next/image';
import {useState} from 'react';
import styles from './Header.module.css';
import Link from "next/link";

const Header = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const closeMenu = () => {
    setIsMenuOpen(false);
  };

  return (
    <header className={styles.header}>
      <div className="container">
        <div className={styles.headerContent}>
          <div className={styles.logoGroup}>
            <Link href="/" className={styles.logoLink} onClick={closeMenu}>
              <Image
                src="/images/logo/podo_logo.svg"
                alt="Podo Logo"
                width={36}
                height={24}
                className={styles.logoSvg}
                priority
              />
              <span className={styles.brandName}>Podo</span>
            </Link>
            <span className={styles.indieBadge}>Indie Craft</span>
          </div>

          <nav className={`${styles.nav} ${isMenuOpen ? styles.navOpen : ''}`}>
            <ul className={styles.navList}>
              <li className={styles.navItem}>
                <Link href="/identity" className={styles.navLink} onClick={closeMenu}>
                  정체성
                </Link>
              </li>
              <li className={styles.navItem}>
                <Link href="/#features" className={styles.navLink} onClick={closeMenu}>
                  기능
                </Link>
              </li>
              <li className={styles.navItem}>
                <Link href="/support" className={styles.navLink} onClick={closeMenu}>
                  고객지원
                </Link>
              </li>
            </ul>

            <div className={styles.navCtaMobile}>
              <Link href="/#download" className={styles.ctaButton} onClick={closeMenu}>
                앱 다운로드
              </Link>
            </div>
          </nav>

          <div className={styles.actions}>
            <Link href="/#download" className={styles.headerCta}>
              <span>다운로드</span>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M7 13l5 5 5-5M12 4v14"/>
              </svg>
            </Link>

            <button
              className={`${styles.menuButton} ${isMenuOpen ? styles.menuOpen : ''}`}
              onClick={toggleMenu}
              aria-label={isMenuOpen ? "메뉴 닫기" : "메뉴 열기"}
            >
              <span className={styles.menuLine}></span>
              <span className={styles.menuLine}></span>
            </button>
          </div>
        </div>
      </div>
      {isMenuOpen && <div className={styles.backdrop} onClick={closeMenu} />}
    </header>
  );
};

export default Header;

